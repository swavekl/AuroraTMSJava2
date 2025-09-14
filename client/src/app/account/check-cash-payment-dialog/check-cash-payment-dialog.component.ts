import {Component, Inject, OnDestroy, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {PaymentRefundService} from '../service/payment-refund.service';
import {PaymentRefund} from '../model/payment-refund.model';
import {PaymentRefundStatus} from '../model/payment-refund-status.enum';
import {Subscription} from 'rxjs';
import {first} from 'rxjs/operators';
import {PaymentRequest} from '../model/payment-request.model';
import {PaymentForm} from '../model/payment-type.enum';
import {RefundRequest} from '../model/refund-request.model';

@Component({
    selector: 'app-check-cash-payment-dialog',
    templateUrl: './check-cash-payment-dialog.component.html',
    styleUrls: ['./check-cash-payment-dialog.component.scss'],
    standalone: false
})
export class CheckCashPaymentDialogComponent  implements OnInit, OnDestroy {

  paymentRequest: PaymentRequest;
  refundRequest: RefundRequest;
  isPayment: boolean = true;

  public OK = 'ok';
  public CANCEL = 'cancel';

  private subscriptions: Subscription = new Subscription();
  checkNumber: number;
  paidAmount: string;
  notes: string;
  paymentInCash: boolean = false;

  constructor(public dialogRef: MatDialogRef<CheckCashPaymentDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private paymentRefundService: PaymentRefundService) {
    this.paymentRequest = data.paymentRequest;
    this.refundRequest = data.refundRequest;
    this.isPayment = data.isPayment;
    const amount = (this.isPayment) ? this.paymentRequest.amount : this.refundRequest.amount;
    this.paidAmount = Number(amount / 100).toFixed(2);
  }

  ngOnDestroy(): void {
  }

  ngOnInit(): void {
  }

  private recordPaymentRefundComplete() {
    const paymentRefund: PaymentRefund = new PaymentRefund();
    if (this.isPayment) {
      paymentRefund.paidAmount = this.paymentRequest.amount;
      paymentRefund.paidCurrency = this.paymentRequest.currencyCode;
      // record the original amount in case we need to refund.  we will need this to calculate refund at today's rates ?
      paymentRefund.amount = this.paymentRequest.amountInAccountCurrency;
      paymentRefund.itemId = this.paymentRequest.transactionItemId;
      paymentRefund.paymentIntentId = 'n/a';
      paymentRefund.paymentRefundFor = this.paymentRequest.paymentRefundFor;
      paymentRefund.status = PaymentRefundStatus.PAYMENT_COMPLETED;
    } else {
      // is refund
      paymentRefund.paidAmount = this.refundRequest.amount;
      paymentRefund.paidCurrency = this.refundRequest.currencyCode;
      paymentRefund.amount = this.refundRequest.amountInAccountCurrency;
      paymentRefund.itemId = this.refundRequest.transactionItemId;
      paymentRefund.paymentIntentId = 'n/a';
      paymentRefund.paymentRefundFor = this.refundRequest.paymentRefundFor;
      paymentRefund.refundId = 'n/a';
      paymentRefund.status = PaymentRefundStatus.REFUND_COMPLETED;
    }
    paymentRefund.transactionDate = new Date();
    paymentRefund.paymentForm = (this.paymentInCash) ? PaymentForm.CASH : PaymentForm.CHECK;
    paymentRefund.checkNumber = (this.paymentInCash) ? 0 : this.checkNumber;
    paymentRefund.note = this.notes;
    const subscription = this.paymentRefundService.recordPaymentComplete(paymentRefund)
      .pipe(first())
      .subscribe(
        () => {
        },
        (error: any) => {
          console.log('error during recording of payment complete' + JSON.stringify(error));
        }
      );
    this.subscriptions.add(subscription);
  }

  onRecordPaymentRefund() {
    this.recordPaymentRefundComplete();
    this.dialogRef.close(this.OK);
  }

  public onCancel(): void {
    this.dialogRef.close(this.CANCEL);
  }
}
