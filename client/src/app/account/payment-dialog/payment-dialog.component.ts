import {Component, Inject, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {distinctUntilChanged, first, tap} from 'rxjs/operators';
import {BehaviorSubject, Observable, Subscription} from 'rxjs';
import {
  ConfirmCardPaymentData, PaymentIntentResult,
  StripeCardCvcElementChangeEvent,
  StripeCardCvcElementOptions,
  StripeCardExpiryElementChangeEvent,
  StripeCardExpiryElementOptions,
  StripeCardNumberElementChangeEvent,
  StripeCardNumberElementOptions,
  StripeElementsOptions,
  StripeElementStyle
} from '@stripe/stripe-js';
import {StripeCardNumberComponent, StripeInstance} from 'ngx-stripe';
import {PaymentDialogData} from './payment-dialog-data';
import {PaymentIntentResponse, PaymentRefundService} from '../service/payment-refund.service';
import {PaymentRefund} from '../model/payment-refund.model';
import {PaymentRequest} from '../model/payment-request.model';
import {PaymentRefundStatus} from '../model/payment-refund-status.enum';
import {PaymentForm} from '../model/payment-type.enum';

/**
 * Dialog for collecting information about the credit card and submitting the purchase
 * It is implemented using Stripe Elements so that credit card field can be separated from
 * the expiry date and CVC fields.
 *
 * test cards:
 * 5555555555554444 - Mastercard
 * 378282246310005 - AMEX
 * 6011111111111117 - Discover
 * 3056930009020004 - Diners club
 * 3566002020360505 - JCB
 *
 * Cards with various errors
 *  https://docs.stripe.com/testing?lang=java&testing-method=card-numbers#use-test-cards
 *
 */
@Component({
    selector: 'app-payment-dialog',
    templateUrl: './payment-dialog.component.html',
    styleUrls: ['./payment-dialog.component.scss'],
    standalone: false
})
export class PaymentDialogComponent implements OnInit, OnDestroy {

  public OK = 'ok';
  public CANCEL = 'cancel';

  private readonly EMPTY_ERROR = ' ';


  @ViewChild(StripeCardNumberComponent)
  card: StripeCardNumberComponent;

  // these styles are duplicated in the scss file
  commonStyles: StripeElementStyle = {
    base: {
      iconColor: '#666EE8',
      color: '#31325F',
      fontFamily: 'Roboto, Helvetica Neue, sans-serif',
      fontSmoothing: 'antialiased',
      fontSize: '19px',
      '::placeholder': {
        color: '#CFD7E0',
      },
    },
    invalid: {
      // match theme's warn color
      color: '#F44336'
    },
    empty: {
      // turn off lightyellow background
      backgroundColor: 'white'
    }
  };

  ccNumCardOptions: StripeCardNumberElementOptions = {
    showIcon: true,
    style: this.commonStyles
  };

  expiryDateOptions: StripeCardExpiryElementOptions = {
    style: this.commonStyles
  };

  cvcCardOptions: StripeCardCvcElementOptions = {
    style: this.commonStyles
  };

  elementsOptions: StripeElementsOptions = {
    locale: 'auto',
  };

  formGroup: UntypedFormGroup;

  // payment in progress indicator
  // private paymentInProgressSubject: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  // paymentInProgress$: Observable<boolean>;

  // plain flag
  paymentInProgress: boolean;

  // Stripe service
  stripeInstance: StripeInstance;
  // payment request data
  paymentRequest: PaymentRequest;

  errorMessage = this.EMPTY_ERROR;

  // if true payment is completed
  paymentComplete: boolean;

  // flags indicating if individual credit card fields are valid
  private creditCardValid: boolean;
  private expirationDateValid: boolean;
  private CVCValid: boolean;
  private postalCodeValid: boolean;
  private nameOnCardValid: boolean;

  // client secret needed to confirm payment
  private clientSecret: string;

  private subscriptions: Subscription = new Subscription();

  // currency in which to pay for this service
  currencyCode = 'USD';

  /**
   *
   * @param dialogRef
   * @param data
   * @param fb
   * @param paymentRefundService
   */
  constructor(public dialogRef: MatDialogRef<PaymentDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: PaymentDialogData,
              private fb: UntypedFormBuilder,
              private paymentRefundService: PaymentRefundService) {
    this.stripeInstance = data.stripeInstance;
    this.paymentRequest = data.paymentRequest;
    this.currencyCode = data.paymentRequest.currencyCode;
    // this.paymentInProgress$ = this.paymentInProgressSubject.asObservable().pipe(distinctUntilChanged());
    this.paymentInProgress = false;
    this.creditCardValid = false;
    this.expirationDateValid = false;
    this.CVCValid = false;
    this.postalCodeValid = false;
    this.nameOnCardValid = false;
    this.paymentComplete = false;
    this.clientSecret = null;
  }

  ngOnInit(): void {
    this.formGroup = this.fb.group({
      nameOnCard: [this.paymentRequest.fullName, Validators.required],
      postalCode: [this.paymentRequest.postalCode, Validators.required]
    });
    // create payment intent while we wait for user to enter credit card
    // information to shave a bit of time (about 1.5 secs) from the whole process
    this.createPaymentIntent();
  }

  private setPaymentInProgress(inProgress: boolean) {
    // this.paymentInProgressSubject.next(inProgress);
    this.paymentInProgress = inProgress;
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  isFormValid(): boolean {
    // the 3 stripe fields are not part of this form group - only name on card and postal code
    // so maintain their status independently
    return (this.clientSecret != null) && this.formGroup.valid &&
      this.creditCardValid && this.expirationDateValid && this.CVCValid;
  }

  /**
   * Gets the payment intent client secret while waiting for user input
   * @private
   */
  private createPaymentIntent() {
    const subscription = this.paymentRefundService.createPaymentIntent(this.paymentRequest)
      .pipe(first())
      .subscribe(
        (pi: PaymentIntentResponse) => {
          this.clientSecret = pi.clientSecret;
        },
        (error: any) => {
          console.log('Unable to initiate payment - ' + JSON.stringify(error));
          this.errorMessage = 'Unable to initiate payment for this account';
        });
    this.subscriptions.add(subscription);
  }

  /**
   * Confirms the payment
   */
  onPay(): void {
    if (this.isFormValid() && !this.paymentInProgress) {
      this.setPaymentInProgress(true);
      // prepare data
      const postalCode = this.formGroup.value['postalCode'];
      const nameOnCard = this.formGroup.value['nameOnCard'];
      const confirmCardPaymentData: ConfirmCardPaymentData = {
        payment_method: {
          card: this.card.element,
          billing_details: {
            name: nameOnCard,
            address: {
              postal_code: postalCode
            }
          }
        }
      };
      // generate direct charge
      this.stripeInstance.confirmCardPayment(this.clientSecret, confirmCardPaymentData)
        .pipe(
          first(),
          tap({
            next: (result: PaymentIntentResult) => {
              if (result.error) {
                // Show error to your customer (e.g., insufficient funds)
                console.log(result.error.message);
                this.errorMessage = result.error.message;
                this.setPaymentInProgress(false);
              } else {
                // The payment has been processed!
                if (result.paymentIntent.status === 'succeeded') {
                  // Show a success message to your customer
                  // or close the dialog
                  this.errorMessage = '';
                  this.recordPaymentComplete(result.paymentIntent.id);
                }
              }
            },
            error: (error: any) => {
              console.log('error creating payment intent' + JSON.stringify(error));
              this.errorMessage = error?.error;
              this.setPaymentInProgress(false);
            }
        }))
        .subscribe();
    }
  }

  public onCancel(): void {
    this.dialogRef.close(this.CANCEL);
  }

  onCreditCardNumberChange(event: StripeCardNumberElementChangeEvent) {
    this.errorMessage = (event?.error) ? event?.error?.message : this.EMPTY_ERROR;
    this.creditCardValid = event?.complete;
  }

  onExpiryChange(event: StripeCardExpiryElementChangeEvent) {
    this.errorMessage = (event?.error) ? event?.error?.message : this.EMPTY_ERROR;
    this.expirationDateValid = event?.complete;
  }

  onCvcChange(event: StripeCardCvcElementChangeEvent) {
    this.errorMessage = (event?.error) ? event?.error?.message : this.EMPTY_ERROR;
    this.CVCValid = event?.complete;
  }

  isNameOnCardValid(): boolean {
    return this?.formGroup?.controls?.nameOnCard.valid === true;
  }

  isPostalCodeValid(): boolean {
    return this?.formGroup?.controls?.postalCode.valid === true;
  }

  /**
   * Records successful payment in our database so we can list it without going to Stripe API
   * @param paymentIntentId payment intent needed in case of a refund
   * @private
   */
  private recordPaymentComplete(paymentIntentId: string) {
    const paymentRefund: PaymentRefund = new PaymentRefund();
    paymentRefund.paidAmount = this.paymentRequest.amount;
    paymentRefund.paidCurrency = this.paymentRequest.currencyCode;
    // record the original amount in case we need to refund.  we will need this to calculate refund at today's rates ?
    paymentRefund.amount = this.paymentRequest.amountInAccountCurrency;
    paymentRefund.itemId = this.paymentRequest.transactionItemId;
    paymentRefund.paymentIntentId = paymentIntentId;
    paymentRefund.paymentRefundFor = this.paymentRequest.paymentRefundFor;
    paymentRefund.status = PaymentRefundStatus.PAYMENT_COMPLETED;
    paymentRefund.transactionDate = new Date();
    paymentRefund.paymentForm = PaymentForm.CREDIT_CARD;
    const subscription = this.paymentRefundService.recordPaymentComplete(paymentRefund)
      .pipe(
        first(),
        tap({
          next: (paymentRefund: PaymentRefund) => {
            this.paymentComplete = true;
            this.errorMessage = 'Success';
            this.setPaymentInProgress(false);
          },
          error: (error: any) => {
            this.paymentComplete = true;
            this.setPaymentInProgress(false);
            console.log('error during recording of payment complete' + JSON.stringify(error));
            this.errorMessage = error;
          }
        })
      ).subscribe();
    this.subscriptions.add(subscription);
  }

  onClose() {
    this.dialogRef.close(this.OK);
  }

  /**
   * decides to show either error message in red or success in green
   */
  isError() {
    return !this.paymentComplete;
  }
}
