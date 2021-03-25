import {PaymentRefundFor} from '../payment-dialog/payment-data';
import {PaymentRefundStatus} from './payment-refund-status.enum';

export class PaymentRefund {

  // stripe payment intent id
  id: number;

  // Stripe payment intent id
  paymentIntentId: string;

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
}
