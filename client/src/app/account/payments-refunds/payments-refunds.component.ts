import {Component, Input} from '@angular/core';
import {PaymentRefund} from '../model/payment-refund.model';
import {PaymentRefundStatus} from '../model/payment-refund-status.enum';

@Component({
    selector: 'app-payments-refunds',
    templateUrl: './payments-refunds.component.html',
    styleUrls: ['./payments-refunds.component.scss'],
    standalone: false
})
export class PaymentsRefundsComponent {
  @Input()
  paymentsRefunds: PaymentRefund[];

  @Input()
  entryTotal: number;

  // player and tournament currencies if different
  @Input()
  tournamentCurrency: string;

  isPayment(paymentRefund: PaymentRefund) {
    return paymentRefund.status === PaymentRefundStatus.PAYMENT_COMPLETED;
  }


  getPaymentsRefundsTotal(): number {
    let paymentsRefundsTotal = 0;
    if (this.paymentsRefunds != null) {
      this.paymentsRefunds.forEach((paymentRefund: PaymentRefund) => {
        const amount: number = paymentRefund.amount / 100;
        if (paymentRefund.status === PaymentRefundStatus.PAYMENT_COMPLETED) {
          paymentsRefundsTotal += amount;
        } else if (paymentRefund.status === PaymentRefundStatus.REFUND_COMPLETED) {
          paymentsRefundsTotal -= amount;
        }
      });
    }
    return paymentsRefundsTotal;
  }

  getBalance() {
    return this.entryTotal - this.getPaymentsRefundsTotal();
  }
}
