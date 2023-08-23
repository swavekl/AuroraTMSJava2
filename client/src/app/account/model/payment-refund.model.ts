import {PaymentRefundStatus} from './payment-refund-status.enum';
import {PaymentRefundFor} from './payment-refund-for.enum';
import {PaymentForm} from './payment-type.enum';

/**
 * Class representing individual payment or refund in our database
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

  // amount actually paid in currency of payment - may be different from tournament currency
  paidAmount: number;

  // currency code in which the paid amount is expressed
  paidCurrency: string;

  // date & time of payment or refund
  transactionDate: Date;

  // indicates if this is payment or refund
  status: PaymentRefundStatus = PaymentRefundStatus.PAYMENT_COMPLETED;

  // what form of payment was used credit card, check or cash
  paymentForm: PaymentForm = PaymentForm.CREDIT_CARD;
}
