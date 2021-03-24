import {Injectable} from '@angular/core';
import {StripeFactoryService, StripeInstance} from 'ngx-stripe';
import {PaymentRefundService} from './payment-refund.service';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {PaymentData} from '../payment-dialog/payment-data';
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
   * @param data payment data - amount, person's name, postal code to prefill the dialog
   */
  public showPaymentDialog(data: PaymentData) {

    // get strip public key
    const key$: Observable<string> = this.paymentRefundService.getStripeKey();
    key$
      .pipe(first())
      .subscribe(
        (publicKey: string) => {
          // console.log('got public key ' + publicKey);
          data.stripeInstance = this.stripeFactoryService.create(publicKey);
          this.doShowDialog(data);
        },
        (error) => {
          console.log('Couldn\'t obtain stripe public key ' + JSON.stringify(error));
        }
      );
  }

  /**
   * Show dialog
   * @param paymentData
   * @private
   */
  private doShowDialog(paymentData: PaymentData) {
    const config: MatDialogConfig = {
      width: '330px', height: '460px', data: paymentData
    };
    // save the scope because it is wiped out in the component
    // so that it is not sent into the http service
    const callbackScope = paymentData.callbackScope;
    paymentData.callbackScope = null;
    const dialogRef = this.dialog.open(PaymentDialogComponent, config);
    dialogRef.afterClosed().subscribe(result => {
      if (result === 'ok') {
        if (paymentData.successCallbackFn != null) {
          paymentData.successCallbackFn(callbackScope);
        }
      } else {
        if (paymentData.cancelCallbackFn != null) {
          paymentData.cancelCallbackFn(callbackScope);
        }
      }
    });
  }
}
