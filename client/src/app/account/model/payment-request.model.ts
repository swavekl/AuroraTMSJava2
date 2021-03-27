import {PaymentRefundFor} from './payment-refund-for.enum';

/**
 * Payment Request
 */
export class PaymentRequest {

  // what this payment is for
  paymentRefundFor: PaymentRefundFor = PaymentRefundFor.TOURNAMENT_ENTRY;

  // item id which identifies the account to which the payment is to be made
  accountItemId: number;

  // item id for which the payment is meant e.g. tournament entry
  transactionItemId: number;

  // amount to pay in e.g. $20.34 will be 2034
  amount: number;

  // 22 chars long descriptor which will appear on the credit card statement
  statementDescriptor: string;

  // name person paying for prefilling Name on the card field
  fullName: string;

  // postal code of person paying
  postalCode: string;

  // email address where to send receipt
  receiptEmail: string;
}
