// type of payment which will determine which account will be used
import {StripeInstance} from 'ngx-stripe';
import {PaymentRequest} from '../model/payment-request.model';

export class PaymentDialogData {
  // actual request data sent to backend
  paymentRequest: PaymentRequest;

  // stripe service instance needed to complete payment
  stripeInstance: StripeInstance;
}

