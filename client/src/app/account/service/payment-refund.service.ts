import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {PaymentData} from '../payment-dialog/payment-data';
import {distinctUntilChanged, map, tap} from 'rxjs/operators';

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

  public createPaymentIntent(data: PaymentData): Observable<PaymentIntentResponse> {
    const url = `/api/paymentrefund/secret`;
    this.setLoading(true);
    return this.httpClient.post<PaymentIntentResponse>(url, data)
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

  /**
   * Gets public key for either testing or live system
   */
  public getStripeKey(): Observable<string> {
    const url = `/api/paymentrefund/publickey`;
    return this.httpClient.get<any>(url)
      .pipe(
        map((response: any) => {
          // console.log ('got public key ' + JSON.stringify(response));
          return response?.stripePublicKey;
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
