import {Component, OnDestroy, OnInit} from '@angular/core';
import {ClubAffiliationApplicationService} from '../service/club-affiliation-application.service';
import {combineLatest, Observable, of, Subscription} from 'rxjs';
import {ClubAffiliationApplication} from '../model/club-affiliation-application.model';
import {LinearProgressBarService} from '../../../shared/linear-progress-bar/linear-progress-bar.service';
import {ActivatedRoute, Router} from '@angular/router';
import {createSelector} from '@ngrx/store';
import {first} from 'rxjs/operators';
import {ClubAffiliationApplicationStatus} from '../model/club-affiliation-application-status';
import {PaymentDialogService} from '../../../account/service/payment-dialog.service';
import {ApplicationAndPayment} from './club-affiliation-application.component';
import {PaymentRequest} from '../../../account/model/payment-request.model';
import {PaymentRefundFor} from '../../../account/model/payment-refund-for.enum';
import {PaymentDialogData} from '../../../account/payment-dialog/payment-dialog-data';
import {CallbackData} from '../../../account/model/callback-data';
import {AuthenticationService} from '../../../user/authentication.service';
import {Profile} from '../../../profile/profile';
import {PaymentRefundService} from '../../../account/service/payment-refund.service';
import {PaymentRefund} from '../../../account/model/payment-refund.model';

@Component({
  selector: 'app-club-affiliation-application-container',
  template: `
    <app-club-affiliation-application
      [clubAffiliationApplication]="clubAffiliationApplication$ | async"
      [paymentsRefunds]="paymentsRefunds$ | async"
      (saved)="onSaved($event)">
    </app-club-affiliation-application>
  `,
  styles: [],
  providers: [
    PaymentDialogService
  ]
})
export class ClubAffiliationApplicationContainerComponent implements OnInit, OnDestroy {

  public clubAffiliationApplication$: Observable<ClubAffiliationApplication>;
  public paymentsRefunds$: Observable<PaymentRefund[]>;

  private loading$: Observable<boolean>;
  private subscriptions: Subscription = new Subscription();
  private applicationId: number;
  private creating: boolean;
  private showingPaymentDialog: boolean;

  constructor(private clubAffiliationApplicationService: ClubAffiliationApplicationService,
              private activatedRoute: ActivatedRoute,
              private paymentDialogService: PaymentDialogService,
              private authenticationService: AuthenticationService,
              private paymentRefundService: PaymentRefundService,
              private router: Router,
              private linearProgressBarService: LinearProgressBarService) {
    const routePath = this.activatedRoute.snapshot.routeConfig.path;
    this.creating = (routePath.indexOf('create') !== -1);
    const strId = this.activatedRoute.snapshot.params['id'] || 0;
    this.applicationId = Number(strId);
    this.showingPaymentDialog = false;
    this.setupProgressIndicator();
    this.loadApplication();
    this.loadPayments();
  }

  private setupProgressIndicator() {
    this.loading$ = combineLatest(
      this.clubAffiliationApplicationService.store.select(this.clubAffiliationApplicationService.selectors.selectLoading),
      this.paymentRefundService.loading$,
      (applicationLoading: boolean, paymentLoading: boolean) => {
        if (this.showingPaymentDialog) {
          // ignore paymentLoading indicator when the dialog is being shown - it is coming from
          // a popup not using the service, not in this page
          return applicationLoading;
        } else {
          return applicationLoading || paymentLoading;
        }
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

  onSaved(applicationAndPayment: ApplicationAndPayment) {
    this.clubAffiliationApplicationService.upsert(applicationAndPayment.clubAffiliationApplication)
      .pipe(first())
      .subscribe(
        (savedClubAffiliationApplication: ClubAffiliationApplication) => {
          this.applicationId = savedClubAffiliationApplication.id;
          if (!applicationAndPayment.payFee) {
            this.goBackToList();
          } else {
            this.showPaymentDialog(savedClubAffiliationApplication);
          }
        },
        (error: any) => {
          console.log('error on save', error);
        });
  }

  private goBackToList() {
    // go back to list
    this.router.navigateByUrl('/ui/clubaffiliation/list');
  }

  private loadApplication() {
    if (this.applicationId !== 0) {
      // load from server or cache
      const selector = createSelector(
        this.clubAffiliationApplicationService.selectors.selectEntityMap,
        (entityMap) => {
          return entityMap[this.applicationId];
        });
      // tournament information will not change just get it once
      this.clubAffiliationApplication$ = this.clubAffiliationApplicationService.store.select(selector);
      const subscription = this.clubAffiliationApplication$
        .subscribe((clubAffiliationApplication: ClubAffiliationApplication) => {
          if (!clubAffiliationApplication) {
            this.clubAffiliationApplicationService.getByKey(this.applicationId);
          } else {
            // clone it so that Angular template driven form can modify the values
            const applicationToEdit: ClubAffiliationApplication = JSON.parse(JSON.stringify(clubAffiliationApplication));
            if (this.creating && this.applicationId !== 0) {
              applicationToEdit.id = null;
              applicationToEdit.affiliationExpirationDate = null;
              applicationToEdit.approvalRejectionNotes = null;
              applicationToEdit.status = ClubAffiliationApplicationStatus.New;
            }
            this.clubAffiliationApplication$ = of(applicationToEdit);
          }
        });
      this.subscriptions.add(subscription);
    } else {
      // new application
      this.clubAffiliationApplication$ = of(new ClubAffiliationApplication());
    }
  }

  private loadPayments() {
    if (this.applicationId !== 0 && !this.creating) {
      const subscription = this.paymentRefundService.listPaymentsRefunds(PaymentRefundFor.CLUB_AFFILIATION_FEE, this.applicationId)
        .pipe(first())
        .subscribe((paymentsRefunds: PaymentRefund[]) => {
          this.paymentsRefunds$ = of(paymentsRefunds);
        });
      this.subscriptions.add (subscription);
    } else {
      this.paymentsRefunds$ = of ([]);
    }
  }

  public showPaymentDialog(clubAffiliationApplication: ClubAffiliationApplication) {
    const currencyOfPayment = 'USD';
    const amount: number = 75 * 100;
    const amountInAccountCurrency: number = amount;
    const currentUserProfile: Profile = this.authenticationService.getCurrentUser().profile;
    const fullName = currentUserProfile.firstName + ' ' + currentUserProfile.lastName;
    const postalCode = currentUserProfile.zipCode;
    const email = currentUserProfile.email;
    const statementDescriptor = 'Club Affiliation Fee for ' + clubAffiliationApplication.name;
    const paymentRequest: PaymentRequest = {
      paymentRefundFor: PaymentRefundFor.CLUB_AFFILIATION_FEE,
      accountItemId: 0, // USATT account id
      transactionItemId: clubAffiliationApplication.id,
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
      scope.clubAffiliationApplicationService.update({
        id: scope.applicationId,
        status: ClubAffiliationApplicationStatus.Completed
      }).pipe(first())
        .subscribe((updatedClubAffiliationApplication: ClubAffiliationApplication) => {
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
