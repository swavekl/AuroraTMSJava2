import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {combineLatest, Observable, of, Subscription} from 'rxjs';
import {first} from 'rxjs/operators';
import {createSelector} from '@ngrx/store';
import {SanctionRequestService} from '../service/sanction-request.service';
import {SanctionRequest, SanctionRequestStatus} from '../model/sanction-request.model';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {PaymentRefundFor} from '../../account/model/payment-refund-for.enum';
import {PaymentRefund} from '../../account/model/payment-refund.model';
import {Profile} from '../../profile/profile';
import {PaymentRequest} from '../../account/model/payment-request.model';
import {PaymentDialogData} from '../../account/payment-dialog/payment-dialog-data';
import {CallbackData} from '../../account/model/callback-data';
import {PaymentDialogService} from '../../account/service/payment-dialog.service';
import {AuthenticationService} from '../../user/authentication.service';
import {PaymentRefundService} from '../../account/service/payment-refund.service';

@Component({
  selector: 'app-sanction-edit-container',
  template: `
    <app-sanction-request-edit [sanctionRequest]="sanctionRequest$ | async"
                               [paymentsRefunds]="paymentsRefunds$ | async"
    (saved)="onSave($event)" (canceled)="onCancel($event)">
    </app-sanction-request-edit>
  `,
  styles: [
  ]
})
export class SanctionRequestEditContainerComponent implements OnInit, OnDestroy {
  public sanctionRequest$: Observable<SanctionRequest>;
  public paymentsRefunds$: Observable<PaymentRefund[]>;

  private loading$: Observable<boolean>;
  private subscriptions: Subscription = new Subscription();
  private sanctionRequestId: number;
  private creating: boolean;
  private showingPaymentDialog: boolean;

  constructor(private activatedRoute: ActivatedRoute,
              private router: Router,
              private linearProgressBarService: LinearProgressBarService,
              private sanctionRequestService: SanctionRequestService,
              private paymentDialogService: PaymentDialogService,
              private authenticationService: AuthenticationService,
              private paymentRefundService: PaymentRefundService,
  ) {
    const routePath = this.activatedRoute.snapshot.routeConfig.path;
    this.creating = (routePath.indexOf('create') !== -1);
    const strId = this.activatedRoute.snapshot.params['id'] || 0;
    this.sanctionRequestId = Number(strId);
    this.setupProgressIndicator();
    this.loadSanctionRequest();
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  private setupProgressIndicator() {
    this.loading$ = combineLatest(
      this.sanctionRequestService.store.select(this.sanctionRequestService.selectors.selectLoading),
      this.paymentRefundService.loading$,
      (sanctionRequestLoading: boolean, paymentLoading: boolean) => {
        if (this.showingPaymentDialog) {
          // ignore paymentLoading indicator when the dialog is being shown - it is coming from
          // a popup not using the service, not in this page
          return sanctionRequestLoading;
        } else {
          return sanctionRequestLoading || paymentLoading;
        }
      }
    );

    const loadingSubscription = this.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
  }

  /**
   *
   * @private
   */
  private loadSanctionRequest() {
    if (this.sanctionRequestId !== 0) {
      const selector = createSelector(
        this.sanctionRequestService.selectors.selectEntityMap,
        (entityMap) => {
          return entityMap[this.sanctionRequestId];
        });

      this.sanctionRequest$ = this.sanctionRequestService.store.select(selector);
      const subscription = this.sanctionRequest$.subscribe((sanctionRequest: SanctionRequest) => {
        if (!sanctionRequest) {
          this.sanctionRequestService.getByKey(this.sanctionRequestId);
        } else {
          // clone it
          const sanctionRequestToEdit: SanctionRequest = JSON.parse(JSON.stringify(sanctionRequest));
          // if making a copy of existing one
          if (this.creating && this.sanctionRequestId !== 0) {
            sanctionRequestToEdit.id = null;
            sanctionRequestToEdit.status = SanctionRequestStatus.New;
            sanctionRequestToEdit.requestDate = new Date();
          }
          this.sanctionRequest$ = of(sanctionRequestToEdit);
        }
      });

      this.subscriptions.add(subscription);

    } else {
      this.sanctionRequest$ = of(new SanctionRequest());
    }
  }

  public onSave(sanctionRequestAndPayment: SanctionRequestAndPayment) {
    const sanctionRequest = sanctionRequestAndPayment.sanctionRequest;
      this.sanctionRequestService.upsert(sanctionRequest)
        .pipe(first())
        .subscribe(
          (savedSanctionRequest: SanctionRequest) => {
            this.sanctionRequestId = savedSanctionRequest.id;
            if (!sanctionRequestAndPayment.payFee) {
              this.goBackToList();
            } else {
              this.showPaymentDialog(savedSanctionRequest);
            }

          }, (error: any) => {
            console.log('error on save', error);
          });
    }

    onCancel($event: any) {
      this.goBackToList();
    }

  private goBackToList() {
      // go back to list
      this.router.navigateByUrl('/sanction/list');
    }

  private loadPayments() {
    if (this.sanctionRequestId !== 0 && !this.creating) {
      const subscription = this.paymentRefundService.listPaymentsRefunds(PaymentRefundFor.CLUB_AFFILIATION_FEE, this.sanctionRequestId)
        .pipe(first())
        .subscribe((paymentsRefunds: PaymentRefund[]) => {
          this.paymentsRefunds$ = of(paymentsRefunds);
        });
      this.subscriptions.add (subscription);
    } else {
      this.paymentsRefunds$ = of ([]);
    }
  }

  public showPaymentDialog(sanctionRequest: SanctionRequest) {
    const currencyOfPayment = 'USD';
    // todo - determine amount to pay
    const amount: number = 75 * 100;
    const amountInAccountCurrency: number = amount;
    const currentUserProfile: Profile = this.authenticationService.getCurrentUser().profile;
    const fullName = currentUserProfile.firstName + ' ' + currentUserProfile.lastName;
    const postalCode = currentUserProfile.zipCode;
    const email = currentUserProfile.email;
    const statementDescriptor = 'Sanction Request Fee for ' + sanctionRequest.tournamentName;
    const paymentRequest: PaymentRequest = {
      paymentRefundFor: PaymentRefundFor.TOURNAMENT_SANCTION_FEE,
      accountItemId: 0, // USATT account id
      transactionItemId: sanctionRequest.id,
      amount: amount,
      currencyCode: currencyOfPayment,
      amountInAccountCurrency: amountInAccountCurrency,
      statementDescriptor: statementDescriptor,
      fullName: fullName,
      postalCode: postalCode,
      receiptEmail: email,
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
    this.paymentDialogService.showPaymentDialog(paymentDialogData, callbackData);
  }

  /**
   * Callback from payment dialog when payment is successful
   */
  onPaymentSuccessful(scope: any) {
    this.showingPaymentDialog = false;
    if (scope != null) {
      scope.sanctionRequestService.update({
        id: scope.sanctionRequestId,
        status: SanctionRequestStatus.Completed
      }).pipe(first())
        .subscribe((updatedSanctionRequest: SanctionRequest) => {
          scope.loadPayments();
        });
    }
  }

  /**
   *
   */
  onPaymentCanceled(scope: any) {
    this.showingPaymentDialog = false;
    // console.log('in onPaymentCanceled');
  }

}

export interface SanctionRequestAndPayment {
  sanctionRequest: SanctionRequest;
  payFee: boolean;
}
