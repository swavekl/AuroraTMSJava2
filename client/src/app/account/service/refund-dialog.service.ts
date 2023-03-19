import {Injectable} from '@angular/core';
import {PaymentRefundService} from './payment-refund.service';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {RefundDialogComponent} from '../refund-dialog/refund-dialog.component';
import {CallbackData} from '../model/callback-data';
import {RefundRequest} from '../model/refund-request.model';

@Injectable({
  providedIn: 'root'
})
export class RefundDialogService {

  constructor(private paymentRefundService: PaymentRefundService,
              private dialog: MatDialog) {
  }

  /**
   * Shows refund dialog to initiate refund process and show status
   * @param refundRequest
   * @param callbackData
   */
  showRefundDialog(refundRequest: RefundRequest, callbackData: CallbackData) {
    const config: MatDialogConfig = {
      width: '330px', height: '260px', data: refundRequest
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
