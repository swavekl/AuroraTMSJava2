import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject, Observable, of, switchMap} from 'rxjs';
import {PaymentRefundFor} from '../model/payment-refund-for.enum';
import {distinctUntilChanged, tap} from 'rxjs/operators';

/**
 * Service for initiating and maintaining shopping cart session
 */
@Injectable({
  providedIn: 'root'
})
export class CartSessionService {
  private indicatorSubject$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  loading$: Observable<boolean>;

  constructor(private httpClient: HttpClient) {
    this.loading$ = this.indicatorSubject$.asObservable().pipe(distinctUntilChanged());
  }

  private setLoading(loading: boolean) {
    this.indicatorSubject$.next(loading);
  }

  public startSession(paymentRefundFor: PaymentRefundFor, objectId: number): Observable<string> {
    this.setLoading(true);
    const url = `/api/cartsession/start/${paymentRefundFor}/${objectId}`;
    return this.httpClient.post(url, '')
      .pipe(
        tap(() => {
            this.setLoading(false);
          },
          () => {
            this.setLoading(false);
          }),
        switchMap((response: any) => {
          // 2e425e03-7994-405d-9fc9-50a5d5c840e7
          // console.log('got cart session response ' + JSON.stringify(response));
          return of(response.sessionUUID);
        })
      );
  }
}
