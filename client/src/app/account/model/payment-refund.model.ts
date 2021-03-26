import {PaymentRefundFor} from '../payment-dialog/payment-data';
import {PaymentRefundStatus} from './payment-refund-status.enum';

/**
 * Class representing individual payment or refund
 */
export class PaymentRefund {

  // stripe payment intent id
  id: number;

  // for payments this is Stripe payment intent id
  // for refunds, then this is payment id from which this refund was made
  paymentIntentId: string;

  // Stripe refund id if this is a refund
  refundId: string;

  // what this payment is for i.e. what is represented by itemId
  paymentRefundFor: PaymentRefundFor;

  // id of a tournament, clinic or something for which we are paying/refunding
  itemId: number;

  // this is decimal expressed as a number $20.34 is 2034
  amount: number;

  // date & time of payment or refund
  transactionDate: Date;

  // status of the payment
  status: PaymentRefundStatus = PaymentRefundStatus.PAYMENT_COMPLETED;

  // Stripe error message
  errorCause: string;
}
