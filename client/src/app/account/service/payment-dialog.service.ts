import {Injectable} from '@angular/core';
import {StripeFactoryService, StripeInstance} from 'ngx-stripe';
import {KeyAccountInfo, PaymentRefundService} from './payment-refund.service';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {CallbackData, PaymentData} from '../payment-dialog/payment-data';
import {Observable} from 'rxjs';
import {first} from 'rxjs/operators';
import {PaymentDialogComponent} from '../payment-dialog/payment-dialog.component';

/**
 * Service which retrieves the Stripe public key dynamically and initilizes Stripe service with it
 * before showing the dialog.
 *
 * Also this service ensures consistent size of dialog from anywhere it is used and calls back the callbacks
 * after successful charge or cancelation.
 */
@Injectable({
  providedIn: 'root'
})
export class PaymentDialogService {

  constructor(private paymentRefundService: PaymentRefundService,
              private stripeFactoryService: StripeFactoryService,
              private dialog: MatDialog) {
  }

  /**
   * Fetch public key and show dialog later
   * @param paymentData payment data - amount, person's name, postal code to prefill the dialog
   * @param callbackData callback data
   */
  public showPaymentDialog(paymentData: PaymentData, callbackData: CallbackData) {

    // get stripe public key
    const keyAccountInfo$: Observable<KeyAccountInfo> = this.paymentRefundService.getKeyAccountInfo(paymentData.accountItemId);
    keyAccountInfo$
      .pipe(first())
      .subscribe(
        (keyAccountInfo: KeyAccountInfo) => {
          // console.log('got public key ' + publicKey);
          paymentData.stripeInstance = this.stripeFactoryService.create(keyAccountInfo.stripePublicKey, {
            stripeAccount: keyAccountInfo.tournamentAccountId  // connected account id
          });
          this.doShowDialog(paymentData, callbackData);
        },
        (error) => {
          console.log('Couldn\'t obtain stripe public key ' + JSON.stringify(error));
        }
      );
  }

  /**
   * Show dialog
   * @param paymentData payment data - amount, person's name, postal code to prefill the dialog
   * @param callbackData callback data
   * @private
   */
  private doShowDialog(paymentData: PaymentData, callbackData: CallbackData) {
    const config: MatDialogConfig = {
      width: '330px', height: '460px', data: paymentData
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
