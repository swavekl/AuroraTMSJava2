import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {createSelector} from '@ngrx/store';
import {BehaviorSubject, combineLatest, expand, Observable, of, Subscription, takeWhile} from 'rxjs';
import {delay, first, switchMap} from 'rxjs/operators';
import {TournamentProcessingRequest} from '../model/tournament-processing-request';
import {TournamentProcessingService} from '../service/tournament-processing.service';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {PaymentDialogService} from '../../account/service/payment-dialog.service';
import {Profile} from '../../profile/profile';
import {PaymentRequest} from '../../account/model/payment-request.model';
import {PaymentRefundFor} from '../../account/model/payment-refund-for.enum';
import {PaymentDialogData} from '../../account/payment-dialog/payment-dialog-data';
import {CallbackData} from '../../account/model/callback-data';
import {AuthenticationService} from '../../user/authentication.service';
import {ProfileService} from '../../profile/profile.service';
import {TournamentProcessingRequestStatus} from '../model/tournament-processing-request-status';

@Component({
  selector: 'app-tournament-processing-detail-container',
  template: `
    <app-tournament-processing-detail [tournamentProcessingRequest]="tournamentProcessingRequest$ | async"
                                      [generatingReports]="reportsGenerating$ | async"
                                      (generateReports)="onGenerateReports($event)"
                                      (submitReports)="onSubmitReports($event)"
                                      (payFee)="onPayTournamentReportFee($event)">
    </app-tournament-processing-detail>
  `,
  styles: []
})
export class TournamentProcessingDetailContainerComponent implements OnInit, OnDestroy {

  private id: number;
  private tournamentId: number;
  private tournamentName: string;

  public tournamentProcessingRequest$: Observable<TournamentProcessingRequest>;

  // loading request data and its supporting detail
  private loading$: Observable<boolean>;
  // generating reports (which takes a few seconds)
  public reportsGenerating$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  // all subscriptions here
  private subscriptions: Subscription = new Subscription();

  // if true we are coming here to submit a set of reports to USATT for processing
  // if false USATT employee is looking at submitted reports
  private submitting: boolean;

  // indicates if we are showing payment dialog
  private showingPaymentDialog: boolean;
  // request being paid for
  private paidForTournamentProcessingRequest: TournamentProcessingRequest;

  private paidForDetailId: number;

  constructor(private tournamentProcessingService: TournamentProcessingService,
              private activatedRoute: ActivatedRoute,
              private router: Router,
              private paymentDialogService: PaymentDialogService,
              private authenticationService: AuthenticationService,
              private profileService: ProfileService,
              private linearProgressBarService: LinearProgressBarService) {
    const routePath = this.activatedRoute.snapshot.routeConfig.path;
    this.submitting = (routePath.indexOf('submit') !== -1);
    const strId = this.activatedRoute.snapshot.params['id'] || 0;
    const strTournamentId = this.activatedRoute.snapshot.params['tournamentId'] || 0;
    this.tournamentName = this.activatedRoute.snapshot.params['tournamentName'] || 'N/A';
    this.id = Number(strId);
    this.tournamentId = Number(strTournamentId);
    this.setupProgressIndicator();
    this.loadTournamentProcessingData();
  }

  private setupProgressIndicator() {
    this.showingPaymentDialog = false;
    this.loading$ = combineLatest(
      this.tournamentProcessingService.store.select(this.tournamentProcessingService.selectors.selectLoading),
      this.reportsGenerating$,
      (requestLoading: boolean, reportsGenerating: boolean) => {
        return requestLoading || reportsGenerating;
      }
    );

    const loadingSubscription = this.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
  }

  ngOnInit(): void {
  }

  ngOnDestroy() {
    this.subscriptions.unsubscribe();
  }

  private loadTournamentProcessingData() {
    const selector = createSelector(
      this.tournamentProcessingService.selectors.selectEntityMap,
      (entityMap) => {
        return entityMap[this.id];
      });
    this.tournamentProcessingRequest$ = this.tournamentProcessingService.store.select(selector);
    const subscription = this.tournamentProcessingRequest$
      .subscribe(
        (tournamentProcessingData: TournamentProcessingRequest) => {
          if (!tournamentProcessingData) {
            if (!this.submitting) {
              // not in cache so read it
              this.tournamentProcessingService.getByKey(this.id);
            } else {
              // else read by tournament id
              const query = `tournamentId=${this.tournamentId}`;
              this.tournamentProcessingService.getWithQuery(query)
                .pipe(
                  switchMap((tpdList: TournamentProcessingRequest[]): Observable<TournamentProcessingRequest> => {
                    let tournamentProcessingDataToEdit = new TournamentProcessingRequest();
                    tournamentProcessingDataToEdit.tournamentId = this.tournamentId;
                    tournamentProcessingDataToEdit.tournamentName = this.tournamentName;
                    if (tpdList && tpdList.length > 0) {
                      const firstOne = tpdList[0];
                      tournamentProcessingDataToEdit = JSON.parse(JSON.stringify(firstOne));
                    }
                    return of(tournamentProcessingDataToEdit);
                  })
                ).subscribe((value: TournamentProcessingRequest) => {
                this.tournamentProcessingRequest$ = of(value);
              });
            }
          } else {
            // clone it so that Angular template driven form can modify the values
            const tournamentProcessingDataToEdit: TournamentProcessingRequest = JSON.parse(JSON.stringify(tournamentProcessingData));
            this.tournamentProcessingRequest$ = of(tournamentProcessingDataToEdit);
          }
        },
        (error: any) => {
          console.log('tournament processing data not found', error);
        });
    this.subscriptions.add(subscription);
  }

  onGenerateReports(tournamentProcessingRequest: TournamentProcessingRequest) {
    this.tournamentProcessingService.upsert(tournamentProcessingRequest)
      .pipe(first())
      .subscribe((saved: TournamentProcessingRequest) => {
        this.id = saved.id;
        this.waitForReportsCompletion(saved.id);
      });
  }

  /**
   * Issue requests for the report request until the files are generated
   * @param requestId
   * @private
   */
  private waitForReportsCompletion(requestId: number) {
    this.reportsGenerating$.next(true);
    const subscription = this.tournamentProcessingService.getByKey(requestId).pipe(
      delay(6000),  // initial delay
      expand((tpRequest: TournamentProcessingRequest) => {
        // expand is effectively recursive - it merges current request with new request
        return this.tournamentProcessingService.getByKey(requestId).pipe(delay(1000));  // subsequent delays
      }),
      takeWhile((tpRequest1: TournamentProcessingRequest) => {
        const reportsReady = this.areReportsReady(tpRequest1);
        if (reportsReady) {
          this.reportsGenerating$.next(false);
        }
        return !reportsReady;
      })
    ).subscribe((result: TournamentProcessingRequest) => {
    });

    this.subscriptions.add(subscription);
  }

  private areReportsReady(tpRequest: TournamentProcessingRequest): boolean {
    let ready = true;
    if (tpRequest != null && tpRequest.details != null) {
      for (let i = 0; i < tpRequest.details.length; i++) {
        const detail = tpRequest.details[i];
        if (detail.createdOn == null) {
          ready = false;
          break;
        }
      }
    }
    return ready;
  }

  onSubmitReports(tournamentProcessingRequest: TournamentProcessingRequest) {
    this.tournamentProcessingService.upsert(tournamentProcessingRequest)
      .pipe(first())
      .subscribe((saved: TournamentProcessingRequest) => {
        this.waitForReportsCompletion(saved.id);
      });
  }

  /**
   * Initiate payment sequence
   * @param event
   */
  onPayTournamentReportFee(event: any) {
    let amount: number;
    let userProfileId: string;
    const details = event.request.details || [];
    for (let i = 0; i < details.length; i++) {
      const detail = details[i];
      if (detail.id === event.detailId) {
        amount = detail.amountToPay;
        userProfileId = detail.createdByProfileId;
        break;
      }
    }
    if (amount > 0) {
      const subscription = this.profileService.getProfile(userProfileId)
        .subscribe((createdByProfile: Profile) => {
          this.showPaymentDialogForUser(createdByProfile, amount, event.currency, event.detailId, event.request);
        });
      this.subscriptions.add(subscription);
    }
  }

  private showPaymentDialogForUser(createdByProfile: Profile,
                                   amount: number,
                                   currencyOfPayment: string,
                                   detailId: number,
                                   tournamentProcessingRequest: TournamentProcessingRequest) {
    const fullName = createdByProfile.firstName + ' ' + createdByProfile.lastName;
    const postalCode = createdByProfile.zipCode;
    const email = createdByProfile.email;
    const amountInAccountCurrency: number = amount;
    const statementDescriptor = 'Tournament Report Fee for ' + tournamentProcessingRequest.tournamentName + ' for detail ' + detailId;
    const paymentRequest: PaymentRequest = {
      paymentRefundFor: PaymentRefundFor.TOURNAMENT_REPORT_FEE,
      accountItemId: 0, // USATT account id
      transactionItemId: detailId,
      amount: amount,
      currencyCode: currencyOfPayment,
      amountInAccountCurrency: amountInAccountCurrency,
      statementDescriptor: statementDescriptor,
      fullName: fullName,
      postalCode: postalCode,
      receiptEmail: email
    };

    const paymentDialogData: PaymentDialogData = {
      paymentRequest: paymentRequest,
      stripeInstance: null
    };

    const callbackData: CallbackData = {
      successCallbackFn: this.onPaymentSuccessful,
      cancelCallbackFn: this.onPaymentCanceled,
      callbackScope: this
    };
    this.showingPaymentDialog = true;
    this.paidForTournamentProcessingRequest = tournamentProcessingRequest;
    this.paidForDetailId = detailId;
    this.paymentDialogService.showPaymentDialog(paymentDialogData, callbackData);
  }


  /**
   * Callback from payment dialog when payment is successful
   */
  onPaymentSuccessful(scope: any) {
    this.showingPaymentDialog = false;
    if (scope != null) {
      const tournamentProcessingRequest = scope.paidForTournamentProcessingRequest;
      const detailId = scope.paidForDetailId;
      const details = tournamentProcessingRequest.details || [];
      for (let i = 0; i < details.length; i++) {
        const detail = details[i];
        if (detail.id === detailId) {
          detail.status = TournamentProcessingRequestStatus.Paid;
          scope.tournamentProcessingService.upsert(tournamentProcessingRequest)
            .pipe(first())
            .subscribe((saved: TournamentProcessingRequest) => {
              console.log('after save paid ', saved);
            });
          break;
        }
      }
    }
  }

  onPaymentCanceled(scope: any) {
    this.showingPaymentDialog = false;
    this.paidForTournamentProcessingRequest = null;
    this.paidForDetailId = null;
  }

}
