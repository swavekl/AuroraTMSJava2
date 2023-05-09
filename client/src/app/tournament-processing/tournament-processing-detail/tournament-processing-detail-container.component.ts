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
import {ProfileService} from '../../profile/profile.service';
import {TournamentProcessingRequestStatus} from '../model/tournament-processing-request-status';
import {TournamentProcessingRequestDetail} from '../model/tournament-processing-request-detail';

@Component({
  selector: 'app-tournament-processing-detail-container',
  template: `
    <app-tournament-processing-detail [tournamentProcessingRequest]="tournamentProcessingRequest$ | async"
                                      [currencyCode]="currencyCode"
                                      [generatingReports]="reportsGenerating$ | async"
                                      (requestEvent)="onRequestEvent($event)">
    </app-tournament-processing-detail>
  `,
  styles: [],
  providers: [
    PaymentDialogService
  ]
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

  // request being paid for
  private paidForTournamentProcessingRequest: TournamentProcessingRequest;

  private paidForDetailId: number;

  // currency to use for payment for tournament report
  public currencyCode: string;

  constructor(private tournamentProcessingService: TournamentProcessingService,
              private activatedRoute: ActivatedRoute,
              private router: Router,
              private paymentDialogService: PaymentDialogService,
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
    this.prepareForPayment();
  }

  private setupProgressIndicator() {
    this.loading$ = combineLatest(
      this.tournamentProcessingService.store.select(this.tournamentProcessingService.selectors.selectLoading),
      this.reportsGenerating$,
      this.paymentDialogService.loading$,
      (requestLoading: boolean, reportsGenerating: boolean, paymentDialogPreparing: boolean) => {
        return requestLoading || reportsGenerating  || paymentDialogPreparing;
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

  private prepareForPayment() {
    this.currencyCode = 'USD';
    this.paymentDialogService.prepareForPayment(PaymentRefundFor.TOURNAMENT_REPORT_FEE, 0)
      .pipe(first())
      .subscribe((accountCurrency: string) => {
        this.currencyCode = accountCurrency;
      });
  }

  onRequestEvent(event: any) {
    switch (event.action) {
      case 'generate':
        this.onGenerateReports(event);
        break;
      case 'submit':
        this.onSubmitReports(event);
        break;
      case 'pay':
        this.onPayTournamentReportFee(event);
        break;
      case 'process':
        this.processRequest(event);
        break;
      case 'delete':
        this.deleteRequest(event);
        break;
    }
  }

  private onGenerateReports(event: any) {
    const tournamentProcessingRequest: TournamentProcessingRequest = event.request;
    const detailId = event.detailId;
    const initialDelay = 1000 * this.getNumReportsToGenerate(tournamentProcessingRequest, detailId);
    this.tournamentProcessingService.upsert(tournamentProcessingRequest)
      .pipe(first())
      .subscribe((saved: TournamentProcessingRequest) => {
        this.id = saved.id;
        this.waitForReportsCompletion(saved.id, initialDelay);
      });
  }


  private processRequest(event: any) {
    const tournamentProcessingRequest: TournamentProcessingRequest = event.request;
    this.tournamentProcessingService.upsert(tournamentProcessingRequest)
      .pipe(first())
      .subscribe((saved: TournamentProcessingRequest) => {
        this.id = saved.id;
        this.waitForReportsCompletion(saved.id, 2000);
      });

  }

  private getNumReportsToGenerate(tournamentProcessingRequest: TournamentProcessingRequest, detailId: number): number {
    let count = 0;
    const details: TournamentProcessingRequestDetail [] = tournamentProcessingRequest.details || [];
    for (let i = 0; i < details.length; i++) {
      const detail = details[i];
      if (detailId === detail.id) {
        count += detail.generatePlayerList ? 1 : 0;
        count += detail.generateTournamentReport ? 1 : 0;
        count += detail.generateApplications ? 1 : 0;
        count += detail.generateMembershipList ? 1 : 0;
        count += detail.generateMatchResults ? 1 : 0;
      }
    }
    return count;
  }

  /**
   * Issue requests for the report request until the files are generated
   * @param requestId
   * @param initialDelay
   * @private
   */
  private waitForReportsCompletion(requestId: number, initialDelay: number) {
    this.reportsGenerating$.next(true);
    const subscription = this.tournamentProcessingService.getByKey(requestId).pipe(
      delay(initialDelay),  // initial delay
      expand((tpRequest: TournamentProcessingRequest) => {
        // expand is effectively recursive - it merges current request with new request
        return this.tournamentProcessingService.getByKey(requestId).pipe(delay(1000));  // subsequent delays
      }),
      takeWhile((tpRequest1: TournamentProcessingRequest) => {
        const reportsReady = this.areReportsReady(tpRequest1);
        if (reportsReady) {
          const tournamentProcessingDataToEdit: TournamentProcessingRequest = JSON.parse(JSON.stringify(tpRequest1));
          this.tournamentProcessingRequest$ = of(tournamentProcessingDataToEdit);
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

  private onSubmitReports(event: any) {
    const tournamentProcessingRequest: TournamentProcessingRequest = event.request;
    const detailId = event.detailId;
    const initialDelay = 1000 * this.getNumReportsToGenerate(tournamentProcessingRequest, detailId);
    this.tournamentProcessingService.upsert(tournamentProcessingRequest)
      .pipe(first())
      .subscribe((saved: TournamentProcessingRequest) => {
        this.waitForReportsCompletion(saved.id, initialDelay);
      });
  }

  /**
   * Initiate payment sequence
   * @param event
   */
  private onPayTournamentReportFee(event: any) {
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
          this.showPaymentDialogForUser(createdByProfile, amount, event.detailId, event.request);
        });
      this.subscriptions.add(subscription);
    }
  }

  private showPaymentDialogForUser(createdByProfile: Profile,
                                   amount: number,
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
      currencyCode: this.currencyCode,
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
    // save the whole request and detail id so we know what we are paying for
    this.paidForTournamentProcessingRequest = tournamentProcessingRequest;
    this.paidForDetailId = detailId;

    this.paymentDialogService.showPaymentDialog(paymentDialogData, callbackData);
  }


  /**
   * Callback from payment dialog when payment is successful
   */
  onPaymentSuccessful(scope: any) {
    if (scope != null) {
      scope.onPaymentSuccessfulInternal(scope.paidForTournamentProcessingRequest, scope.paidForDetailId);
    }
  }

  onPaymentSuccessfulInternal(tournamentProcessingRequest: TournamentProcessingRequest, detailId: number) {
    const details = tournamentProcessingRequest.details || [];
    for (let i = 0; i < details.length; i++) {
      const detail = details[i];
      if (detail.id === detailId) {
        detail.status = TournamentProcessingRequestStatus.Paid;
        detail.paidOn = new Date();
        this.tournamentProcessingService.upsert(tournamentProcessingRequest)
          .pipe(first())
          .subscribe((saved: TournamentProcessingRequest) => {
            const tournamentProcessingDataToEdit: TournamentProcessingRequest = JSON.parse(JSON.stringify(saved));
            this.tournamentProcessingRequest$ = of(tournamentProcessingDataToEdit);
          });
        break;
      }
    }
  }

  onPaymentCanceled(scope: any) {
    this.paidForTournamentProcessingRequest = null;
    this.paidForDetailId = null;
  }

  private deleteRequest(event: any) {
    const tournamentProcessingRequest: TournamentProcessingRequest = event.request;
      let subscription = this.tournamentProcessingService.update(tournamentProcessingRequest)
        .pipe(first())
        .subscribe((saved) => {
          const tournamentProcessingDataToEdit: TournamentProcessingRequest = JSON.parse(JSON.stringify(saved));
          this.tournamentProcessingRequest$ = of(tournamentProcessingDataToEdit);
        });
      this.subscriptions.add(subscription);
  }
}
