import {Component, Inject, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {distinctUntilChanged, first, switchMap} from 'rxjs/operators';
import {BehaviorSubject, Observable, Subscription} from 'rxjs';
import {
  StripeCardCvcElementChangeEvent,
  StripeCardCvcElementOptions,
  StripeCardExpiryElementChangeEvent,
  StripeCardExpiryElementOptions,
  StripeCardNumberElementChangeEvent,
  StripeCardNumberElementOptions,
  StripeElementsOptions,
  StripeElementStyle
} from '@stripe/stripe-js';
import {StripeCardNumberComponent, StripeFactoryService, StripeInstance} from 'ngx-stripe';
import {PaymentData} from './payment-data';
import {PaymentIntentResponse, PaymentRefundService} from '../service/payment-refund.service';
import {PaymentRefund} from '../model/payment-refund.model';
import {PaymentRefundStatus} from '../model/payment-refund-status.enum';

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
 */
@Component({
  selector: 'app-payment-dialog',
  templateUrl: './payment-dialog.component.html',
  styleUrls: ['./payment-dialog.component.scss']
})
export class PaymentDialogComponent implements OnInit, OnDestroy {

  public OK = 'ok';
  public CANCEL = 'cancel';

  private readonly EMPTY_ERROR = ' ';

  // Stripe service
  public stripeInstance: StripeInstance;

  public errorMessage = this.EMPTY_ERROR;
  public paymentComplete: boolean;

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

  formGroup: FormGroup;

  paymentInProgressSubject: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  paymentInProgress$: Observable<boolean>;

  private creditCardValid: boolean;
  private expirationDateValid: boolean;
  private CVCValid: boolean;
  private postalCodeValid: boolean;
  private nameOnCardValid: boolean;

  private subscriptions: Subscription = new Subscription();

  // currency in which to pay for this service
  currencyCode = 'USD';

  /**
   *
   * @param dialogRef
   * @param data
   * @param fb
   * @param paymentRefundService
   * @param stripeFactoryService
   */
  constructor(public dialogRef: MatDialogRef<PaymentDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: PaymentData,
              private fb: FormBuilder,
              private paymentRefundService: PaymentRefundService,
              private stripeFactoryService: StripeFactoryService) {
    this.stripeInstance = data.stripeInstance;
    this.paymentInProgress$ = this.paymentInProgressSubject.asObservable().pipe(distinctUntilChanged());
    this.creditCardValid = false;
    this.expirationDateValid = false;
    this.CVCValid = false;
    this.postalCodeValid = false;
    this.nameOnCardValid = false;
    this.paymentComplete = false;
  }

  ngOnInit(): void {
    this.formGroup = this.fb.group({
      nameOnCard: [this.data.fullName, Validators.required],
      postalCode: [this.data.postalCode, Validators.required]
    });
  }

  private setPaymentInProgress(inProgress: boolean) {
    this.paymentInProgressSubject.next(inProgress);
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  isFormValid(): boolean {
    // the 3 stripe fields are not part of this form group - only name on card and postal code
    // so maintain their status independently
    return this.formGroup.valid &&
      this.creditCardValid && this.expirationDateValid && this.CVCValid;
  }

  onPay(): void {
    // retrieve credit card data and generate charge
    if (this.isFormValid()) {
      const postalCode = this.formGroup.value['postalCode'];
      const nameOnCard = this.formGroup.value['nameOnCard'];
      this.setPaymentInProgress(true);
      this.paymentRefundService.createPaymentIntent(this.data)
        .pipe(
          switchMap((pi: PaymentIntentResponse) =>
            this.stripeInstance.confirmCardPayment(pi.clientSecret, {
              payment_method: {
                card: this.card.element,
                billing_details: {
                  name: nameOnCard,
                  address: {
                    postal_code: postalCode
                  }
                }
              }
            })
          )
        )
        .subscribe((result) => {
            this.setPaymentInProgress(false);
            if (result.error) {
              // Show error to your customer (e.g., insufficient funds)
              console.log(result.error.message);
              this.errorMessage = result.error.message;
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
          (error: any) => {
            console.log('error creating payment intent' + JSON.stringify(error));
            this.setPaymentInProgress(false);
            this.errorMessage = error?.error;
          });
    } else {
      console.log(this.formGroup);
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
   *
   * @param paymentIntentId
   * @private
   */
  private recordPaymentComplete(paymentIntentId: string) {
    const paymentRefund: PaymentRefund = new PaymentRefund();
    paymentRefund.amount = this.data.amount;
    paymentRefund.itemId = this.data.subItemId;
    paymentRefund.paymentIntentId = paymentIntentId;
    paymentRefund.paymentRefundFor = this.data.paymentRefundFor;
    paymentRefund.status = PaymentRefundStatus.PAYMENT_COMPLETED;
    paymentRefund.transactionDate = new Date();
    this.paymentRefundService.recordPaymentComplete(paymentRefund)
      .pipe(first())
      .subscribe(
        () => {
          this.paymentComplete = true;
          this.errorMessage = 'Success';
        },
        (error: any) => {
          console.log('error during recording of payment complete' + JSON.stringify(error));
          this.errorMessage = error;
        }
      );
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
