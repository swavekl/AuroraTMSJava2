import {Injectable} from '@angular/core';
import {PaymentRefundService} from './payment-refund.service';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {CallbackData, RefundData} from '../payment-dialog/payment-data';
import {RefundDialogComponent} from '../refund-dialog/refund-dialog.component';

@Injectable({
  providedIn: 'root'
})
export class RefundDialogService {

  constructor(private paymentRefundService: PaymentRefundService,
              private dialog: MatDialog) {
  }

  /**
   * Shows refund dialog to initiate refund process and show status
   * @param refundData
   * @param callbackData
   */
  showRefundDialog(refundData: RefundData, callbackData: CallbackData) {
    const config: MatDialogConfig = {
      width: '330px', height: '240px', data: refundData
    };

    const callbackScope = callbackData.callbackScope;
    const dialogRef = this.dialog.open(RefundDialogComponent, config);
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
