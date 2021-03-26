// type of payment which will determine which account will be used
import {StripeInstance} from 'ngx-stripe';

export enum PaymentRefundFor {
  TOURNAMENT_ENTRY,   // tournament entry
  CLINIC,             // a clinic
  USATT_FEE           // payment to USATT
}

/**
 * Data for payment
 */
export class PaymentData {

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

  // stripe service Instance
  stripeInstance: StripeInstance;
}

/**
 * class used for requesting refunds
 */
export class RefundData {
  // what this payment is for
  paymentRefundFor: PaymentRefundFor = PaymentRefundFor.TOURNAMENT_ENTRY;

  // item id which identifies the account to which the payment is to be made
  accountItemId: number;

  // item id for which the payment is meant e.g. tournament entry
  transactionItemId: number;

  // amount to pay in e.g. $20.34 will be 2034
  amount: number;
}

/**
 * Additional data used for calling back client
 */
export class CallbackData {
  // success and failure callbacks
  successCallbackFn: (scope: any) => void;
  cancelCallbackFn: (scope: any) => void;

  // object who has the callback functions
  callbackScope: any;
}

