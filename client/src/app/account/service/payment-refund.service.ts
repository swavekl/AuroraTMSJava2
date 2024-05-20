import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {distinctUntilChanged, map, tap} from 'rxjs/operators';
import {PaymentRefund} from '../model/payment-refund.model';
import {RefundRequest} from '../model/refund-request.model';
import {PaymentRequest} from '../model/payment-request.model';
import {PaymentRefundFor} from '../model/payment-refund-for.enum';

/**
 * Service for initiating Stripe charges with payment intent and
 */
@Injectable({
  providedIn: 'root'
})
export class PaymentRefundService {

  private indicatorSubject$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  loading$: Observable<boolean>;

  constructor(private httpClient: HttpClient) {
    this.loading$ = this.indicatorSubject$.asObservable().pipe(distinctUntilChanged());
  }

  private setLoading(loading: boolean) {
    this.indicatorSubject$.next(loading);
  }

  public createPaymentIntent(paymentRequest: PaymentRequest): Observable<PaymentIntentResponse> {
    const url = `/api/paymentrefund/secret`;
    this.setLoading(true);
    return this.httpClient.post<PaymentIntentResponse>(url, paymentRequest)
      .pipe(
        tap(() => {
            this.setLoading(false);
          },
          (error: any) => {
            this.setLoading(false);
          }),
        map((response: PaymentIntentResponse) => {
          // console.log('got payment intent response ' + JSON.stringify(response));
            return response;
          }
        )
      );
  }

  issueRefund(refundRequest: RefundRequest): Observable<RefundResponse> {
    const url = `/api/paymentrefund/issuerefund`;
    this.setLoading(true);
    return this.httpClient.post<RefundResponse>(url, refundRequest)
      .pipe(
        tap(() => {
            this.setLoading(false);
          },
          (error: any) => {
            this.setLoading(false);
          }),
        map((response: RefundResponse) => {
            // console.log('got refund response ' + JSON.stringify(response));
            return response;
          }
        )
      );
  }

  /**
   *
   */
  public recordPaymentComplete(paymentRefund: PaymentRefund) {
    const url = `/api/paymentrefund/recordpayment`;
    this.setLoading(true);
    return this.httpClient.post<PaymentRefund>(url, paymentRefund)
      .pipe(
        tap(() => {
            this.setLoading(false);
          },
          (error: any) => {
            this.setLoading(false);
          }),
        map((response: PaymentRefund) => {
            // console.log('got payment refund response ' + JSON.stringify(response));
            return response;
          }
        )
      );
  }

  /**
   * Gets public key for either testing or live system
   */
  public getKeyAccountInfo(paymentFor: PaymentRefundFor, accountItemId: number): Observable<KeyAccountInfo> {
    const url = `/api/paymentrefund/keyaccountinfo/${paymentFor}/${accountItemId}`;
    return this.httpClient.get<KeyAccountInfo>(url)
      .pipe(
        map((response: KeyAccountInfo) => {
          // console.log ('got KeyAccountInfo ' + JSON.stringify(response));
          return response;
        })
      );
  }

  /**
   * Gets payments refunds for given type of item and item id
   */
  public listPaymentsRefunds(paymentFor: PaymentRefundFor, accountItemId: number): Observable<PaymentRefund[]> {
    this.setLoading(true);
    const url = `/api/paymentrefund/list/${paymentFor}/${accountItemId}`;
    return this.httpClient.get<PaymentRefund[]>(url)
      .pipe(
        tap(() => {
            this.setLoading(false);
          },
          (error: any) => {
            this.setLoading(false);
          }),
        map((response: PaymentRefund[]) => {
          // console.log ('got payment refunds ' + JSON.stringify(response));
          return response;
        })
      );
  }

  /**
   * Gets payments refunds for given type of item and event type with id (tournament, clinic)
   */
  public listPaymentsRefundsForEvent(paymentFor: PaymentRefundFor, eventId: number): Observable<PaymentRefundInfo[]> {
    this.setLoading(true);
    const url = `/api/paymentrefund/listforevent/${paymentFor}/${eventId}`;
    return this.httpClient.get<PaymentRefundInfo[]>(url)
      .pipe(
        tap(() => {
            this.setLoading(false);
          },
          (error: any) => {
            this.setLoading(false);
          }),
        map((response: PaymentRefundInfo[]) => {
          // console.log ('got ALL payment refunds ' + JSON.stringify(response));
          return response;
        })
      );
  }
}

/**
 * Response from payment intent
 */
export interface PaymentIntentResponse {
  clientSecret: string;
}

/**
 * Refund response
 */
export interface RefundResponse {
  // ids of PaymentRefund objects representing only refunded objects
  refunds: number [];
}

/**
 * response from KeyAccountInfo, this is translated from a map of values
 */
export interface KeyAccountInfo {
  // public key associated with main account
  stripePublicKey: string;

  // id of the connected account
  stripeAccountId: string;

  // default account currency code e.g. 'USD'
  defaultAccountCurrency: string;
}

export interface PaymentRefundInfo {
  // id of person who made these payments
  profileId: string;

  // full name of person
  fullName: string;

  // list of payments and refunds
  paymentRefundList: PaymentRefund [];
}

