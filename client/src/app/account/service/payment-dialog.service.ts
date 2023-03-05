import {Injectable} from '@angular/core';
import {StripeFactoryService, StripeInstance} from 'ngx-stripe';
import {KeyAccountInfo, PaymentRefundService} from './payment-refund.service';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {PaymentDialogData} from '../payment-dialog/payment-dialog-data';
import {BehaviorSubject, Observable} from 'rxjs';
import {distinctUntilChanged, first, map, tap} from 'rxjs/operators';
import {PaymentDialogComponent} from '../payment-dialog/payment-dialog.component';
import {CallbackData} from '../model/callback-data';
import {PaymentRefundFor} from '../model/payment-refund-for.enum';

/**
 * Service which retrieves the Stripe public key dynamically and initializes Stripe service with it
 * before showing the dialog.
 *
 * Also this service ensures consistent size of dialog from anywhere it is used and calls back the callbacks
 * after successful charge or cancellation.
 */
@Injectable()
export class PaymentDialogService {

  // Stripe service instance for specific account, we need this created before payment dialog shows because
  // it is required for Elements initialization
  private stripeInstance: StripeInstance;

  // code of account currency
  private defaultAccountCurrency: string;

  // loading indicator for fetching account information
  public loading$: Observable<boolean>;
  private indicatorSubject$: BehaviorSubject<boolean> = new BehaviorSubject(false);

  constructor(private paymentRefundService: PaymentRefundService,
              private stripeFactoryService: StripeFactoryService,
              private dialog: MatDialog) {
    this.stripeInstance = null;
    this.defaultAccountCurrency = 'USD';
    this.loading$ = this.indicatorSubject$.asObservable().pipe(distinctUntilChanged());
  }

  private setLoading(loading: boolean) {
    this.indicatorSubject$.next(loading);
  }

  /**
   * Prepares for payment by fetching account id and currency for given account
   * @param paymentFor what this payment is for
   * @param accountItemId id of tournament or clinic or whatever
   */
  public prepareForPayment(paymentFor: PaymentRefundFor, accountItemId: number): Observable<string> {
    this.setLoading(true);
    // get stripe public key
    return this.paymentRefundService.getKeyAccountInfo(paymentFor, accountItemId)
      .pipe(
        map(
          (keyAccountInfo: KeyAccountInfo) => {
            // console.log('got public key ' + publicKey);
            this.stripeInstance = this.stripeFactoryService.create(keyAccountInfo.stripePublicKey, {
              stripeAccount: keyAccountInfo.stripeAccountId  // connected account id
            });
            this.defaultAccountCurrency = keyAccountInfo?.defaultAccountCurrency.toUpperCase();
            return this.defaultAccountCurrency;
          }
        ),
        tap(() => {
            this.setLoading(false);
          },
          (error: any) => {
            this.setLoading(false);
          }
        )
      );
  }

  /**
   * Fetch public key and show dialog later
   * @param paymentDialogData payment data - amount, person's name, postal code to prefill the dialog
   * @param callbackData callback data
   */
  public showPaymentDialog(paymentDialogData: PaymentDialogData, callbackData: CallbackData) {

    if (this.stripeInstance != null) {
      // console.log('already got stripe instance - showing dialog', this.stripeInstance);
      paymentDialogData.stripeInstance = this.stripeInstance;
      this.doShowDialog(paymentDialogData, callbackData);
    } else {
      this.setLoading(true);
      // get stripe public key
      this.paymentRefundService.getKeyAccountInfo(paymentDialogData.paymentRequest.paymentRefundFor,
        paymentDialogData.paymentRequest.accountItemId)
        .pipe(first())
        .subscribe(
          (keyAccountInfo: KeyAccountInfo) => {
            // console.log('got public key ' + keyAccountInfo.stripePublicKey);
            this.stripeInstance = this.stripeFactoryService.create(keyAccountInfo.stripePublicKey, {
              stripeAccount: keyAccountInfo.stripeAccountId  // connected account id
            });
            // console.log('got stripe instance', this.stripeInstance);
            paymentDialogData.stripeInstance = this.stripeInstance;
            this.setLoading(false);
            this.doShowDialog(paymentDialogData, callbackData);
          },
          (error) => {
            this.setLoading(false);
            console.log('Couldn\'t obtain stripe public key ' + JSON.stringify(error));
          }
        );
    }
  }

  /**
   * Show dialog
   * @param paymentDialogData payment data - amount, person's name, postal code to prefill the dialog
   * @param callbackData callback data
   * @private
   */
  private doShowDialog(paymentDialogData: PaymentDialogData, callbackData: CallbackData) {
    const config: MatDialogConfig = {
      width: '330px', height: '460px', data: paymentDialogData
    };
    // save the scope because it is wiped out in the component
    // so that it is not sent into the http service
    const callbackScope = callbackData.callbackScope;
    const dialogRef = this.dialog.open(PaymentDialogComponent, config);
    dialogRef.afterClosed().subscribe(result => {
      if (result === 'ok') {
        if (callbackData.successCallbackFn != null) {
          callbackData.successCallbackFn(callbackScope);
        }
      } else {
        if (callbackData.cancelCallbackFn != null) {
          callbackData.cancelCallbackFn(callbackScope);
        }
      }
    });
  }
}
