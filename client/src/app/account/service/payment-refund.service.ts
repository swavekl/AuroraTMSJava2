import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {distinctUntilChanged, map, tap} from 'rxjs/operators';
import {PaymentRefund} from '../model/payment-refund.model';
import {RefundRequest} from '../model/refund-request.model';
import {PaymentRequest} from '../model/payment-request.model';

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
          console.log('got payment intent response ' + JSON.stringify(response));
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
            console.log('got payment refund response ' + JSON.stringify(response));
            return response;
          }
        )
      );
  }

  /**
   * Gets public key for either testing or live system
   */
  public getKeyAccountInfo(tournamentId: number): Observable<KeyAccountInfo> {
    const url = `/api/paymentrefund/keyaccountinfo/${tournamentId}`;
    return this.httpClient.get<KeyAccountInfo>(url)
      .pipe(
        map((response: KeyAccountInfo) => {
          console.log ('got KeyAccountInfo ' + JSON.stringify(response));
          return response;
        })
      );
  }

  /**
   * Gets public key for either testing or live system
   */
  public listTournamentPaymentsRefunds(tournamentEntryId: number): Observable<PaymentRefund[]> {
    const url = `/api/paymentrefund/listforentry/${tournamentEntryId}`;
    return this.httpClient.get<PaymentRefund[]>(url)
      .pipe(
        map((response: PaymentRefund[]) => {
          console.log ('got payment refunds ' + JSON.stringify(response));
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
 * response from KeyAccountInfo
 */
export interface KeyAccountInfo {
  // public key associated with main account
  stripePublicKey: string;

  // id of the connected account
  tournamentAccountId: string;

  // default account currency code e.g. 'USD'
  defaultAccountCurrency: string;
}

