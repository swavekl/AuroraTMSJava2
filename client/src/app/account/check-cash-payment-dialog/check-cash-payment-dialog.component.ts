import {Component, Inject, OnDestroy, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {PaymentDialogData} from '../payment-dialog/payment-dialog-data';
import {PaymentRefundService} from '../service/payment-refund.service';
import {PaymentRefund} from '../model/payment-refund.model';
import {PaymentRefundStatus} from '../model/payment-refund-status.enum';
import {Subscription} from 'rxjs';
import {first} from 'rxjs/operators';
import {PaymentRequest} from '../model/payment-request.model';
import {PaymentForm} from '../model/payment-type.enum';

@Component({
  selector: 'app-check-cash-payment-dialog',
  templateUrl: './check-cash-payment-dialog.component.html',
  styleUrls: ['./check-cash-payment-dialog.component.scss']
})
export class CheckCashPaymentDialogComponent  implements OnInit, OnDestroy {

  paymentRequest: PaymentRequest;

  public OK = 'ok';
  public CANCEL = 'cancel';

  private subscriptions: Subscription = new Subscription();
  checkNumber: number;
  paidAmount: string;
  notes: string;
  paymentInCash: boolean = false;

  constructor(public dialogRef: MatDialogRef<CheckCashPaymentDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: PaymentDialogData,
              private paymentRefundService: PaymentRefundService) {
    this.paymentRequest = data.paymentRequest;
    this.paidAmount = Number(this.paymentRequest.amount / 100).toFixed(2);
  }

  ngOnDestroy(): void {
  }

  ngOnInit(): void {
  }

  private recordPaymentComplete() {
    const paymentRefund: PaymentRefund = new PaymentRefund();
    paymentRefund.paidAmount = this.paymentRequest.amount;
    paymentRefund.paidCurrency = this.paymentRequest.currencyCode;
    // record the original amount in case we need to refund.  we will need this to calculate refund at today's rates ?
    paymentRefund.amount = this.paymentRequest.amountInAccountCurrency;
    paymentRefund.itemId = this.paymentRequest.transactionItemId;
    paymentRefund.paymentIntentId = 'n/a';
    paymentRefund.paymentRefundFor = this.paymentRequest.paymentRefundFor;
    paymentRefund.status = PaymentRefundStatus.PAYMENT_COMPLETED;
    paymentRefund.transactionDate = new Date();
    paymentRefund.paymentForm = (this.paymentInCash) ? PaymentForm.CASH : PaymentForm.CHECK;
    paymentRefund.checkNumber = (this.paymentInCash) ? 0 : this.checkNumber;
    paymentRefund.note = this.notes;
    const subscription = this.paymentRefundService.recordPaymentComplete(paymentRefund)
      .pipe(first())
      .subscribe(
        () => {
          // this.paymentComplete = true;
          // this.errorMessage = 'Success';
          // this.setPaymentInProgress(false);
        },
        (error: any) => {
          console.log('error during recording of payment complete' + JSON.stringify(error));
          // this.errorMessage = error;
        }
      );
    this.subscriptions.add(subscription);
  }

  onRecordPayment() {
    this.recordPaymentComplete();
    this.dialogRef.close(this.OK);
  }

  public onCancel(): void {
    this.dialogRef.close(this.CANCEL);
  }
}
