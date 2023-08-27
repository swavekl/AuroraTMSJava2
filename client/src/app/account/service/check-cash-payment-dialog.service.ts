import {Injectable} from '@angular/core';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {PaymentDialogData} from '../payment-dialog/payment-dialog-data';
import {CallbackData} from '../model/callback-data';
import {CheckCashPaymentDialogComponent} from '../check-cash-payment-dialog/check-cash-payment-dialog.component';
import {RefundRequest} from '../model/refund-request.model';

@Injectable({
  providedIn: 'root'
})
export class CheckCashPaymentDialogService {

  constructor(private dialog: MatDialog) { }

  public showPaymentDialog(paymentDialogData: PaymentDialogData, callbackData: CallbackData) {
    const config: MatDialogConfig = {
      width: '440px', height: '370px', data: {isPayment: true, paymentRequest: paymentDialogData.paymentRequest}
    };
    // save the scope because it is wiped out in the component
    // so that it is not sent into the http service
    const callbackScope = callbackData.callbackScope;
    const dialogRef = this.dialog.open(CheckCashPaymentDialogComponent, config);
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

  showRefundDialog(refundRequest: RefundRequest, callbackData: CallbackData) {
    const config: MatDialogConfig = {
      width: '440px', height: '370px', data: {isPayment: false, refundRequest: refundRequest}
    };
    // save the scope because it is wiped out in the component
    // so that it is not sent into the http service
    const callbackScope = callbackData.callbackScope;
    const dialogRef = this.dialog.open(CheckCashPaymentDialogComponent, config);
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
