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

  // amount to pay in e.g. $20.34 will be 2034, this amount is in currencyCode currency - presentment currency
  amount: number;

  // currency in which to charge
  currencyCode: string;

  // amount in the currency of the account e.g. USD if the tournament is in USA as opposed
  // to amount which is in Canadian dollars if player chooses to pay in Canadian $
  amountInAccountCurrency: number;

  // 22 chars long descriptor which will appear on the credit card statement
  statementDescriptor: string;

  // name person paying for prefilling Name on the card field
  fullName: string;

  // postal code of person paying
  postalCode: string;

  // email address where to send receipt
  receiptEmail: string;
}
