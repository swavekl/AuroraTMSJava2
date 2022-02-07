import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, of, switchMap} from 'rxjs';
import {PaymentRefundFor} from '../model/payment-refund-for.enum';

/**
 * Service for initiating and maintaining shopping cart session
 */
@Injectable({
  providedIn: 'root'
})
export class CartSessionService {

  constructor(private httpClient: HttpClient) {
  }

  public startSession(paymentRefundFor: PaymentRefundFor): Observable<string> {
    const url = `/api/cartsession/start/${paymentRefundFor}`;
    return this.httpClient.post(url, '')
      .pipe(
        switchMap((response: any) => {
          // 2e425e03-7994-405d-9fc9-50a5d5c840e7
          // console.log('got cart session response ' + JSON.stringify(response));
          return of(response.sessionUUID);
        })
      );
  }
}
