import {PaymentRefundFor} from './payment-refund-for.enum';

/**
 * class used for requesting refunds
 */
export class RefundRequest {
  // what this payment is for
  paymentRefundFor: PaymentRefundFor = PaymentRefundFor.TOURNAMENT_ENTRY;

  // item id which identifies the account to which the payment is to be made
  accountItemId: number;

  // item id for which the payment is meant e.g. tournament entry
  transactionItemId: number;

  // amount to pay in e.g. $20.34 will be 2034
  amount: number;
}
