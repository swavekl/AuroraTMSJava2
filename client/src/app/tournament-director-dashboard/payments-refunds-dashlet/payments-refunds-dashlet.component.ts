import {Component, Input, OnChanges, OnInit, SimpleChange, SimpleChanges} from '@angular/core';
import {PaymentRefundInfo} from '../../account/service/payment-refund.service';
import {PaymentRefund} from '../../account/model/payment-refund.model';
import {PaymentRefundStatus} from '../../account/model/payment-refund-status.enum';

@Component({
  selector: 'app-payments-refunds-dashlet',
  templateUrl: './payments-refunds-dashlet.component.html',
  styleUrls: ['./payments-refunds-dashlet.component.scss']
})
export class PaymentsRefundsDashletComponent implements OnInit, OnChanges {

  @Input()
  paymentRefundInfos: PaymentRefundInfo[] = [];

  tournamentCurrency: string;
  // totals
  totalPayments: number;
  totalRefunds: number;
  grandTotal: number;

  constructor() {
    this.tournamentCurrency = 'USD';
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    const paymentRefundChanges: SimpleChange = changes.paymentRefundInfos;
    if (paymentRefundChanges) {
      const prInfos = paymentRefundChanges.currentValue;
      let totalPayments = 0;
      let totalRefunds = 0;
      if (prInfos) {
        for (let i = 0; i < prInfos.length; i++) {
          const prInfo: PaymentRefundInfo = prInfos[i];
          const playerPaymentRefundList = prInfo.paymentRefundList;
          for (const paymentRefund of playerPaymentRefundList) {
            if (this.isPayment(paymentRefund)) {
              totalPayments += paymentRefund.amount;
            } else {
              totalRefunds += paymentRefund.amount;
            }
          }
        }
      }
      this.totalPayments = totalPayments;
      this.totalRefunds = totalRefunds;
      this.grandTotal = this.totalPayments - this.totalRefunds;
    }
  }

  isPayment(paymentRefund: PaymentRefund) {
    return paymentRefund.status === PaymentRefundStatus.PAYMENT_COMPLETED;
  }
}
