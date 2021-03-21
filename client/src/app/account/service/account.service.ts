import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject, Observable} from 'rxjs';
import {distinctUntilChanged, map, tap} from 'rxjs/operators';

/**
 * Service for interacting with Stripe account on-boarding process
 */
@Injectable({
  providedIn: 'root'
})
export class AccountService {

  private indicatorSubject$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
  loading$: Observable<boolean>;

  constructor(private httpClient: HttpClient) {
    this.loading$ = this.indicatorSubject$.asObservable().pipe(distinctUntilChanged());
  }

  private setLoading(loading: boolean) {
    this.indicatorSubject$.next(loading);
  }

  /**
   * Checks the status of user connected account
   * @param userProfileId
   */
  public getAccountStatus(userProfileId: string): Observable<AccountStatus> {
    const url = `/api/account/status/${userProfileId}`;
    this.setLoading(true);
    return this.httpClient.get<AccountStatus>(url)
      .pipe(
        tap(() => {
            this.setLoading(false);
          },
          (error: any) => {
            this.setLoading(false);
          }),
        map((response: AccountStatus) => {
            console.log('response ' + JSON.stringify(response));
            return response;
          }
        )
      );
  }

  /**
   * Creates Stripe connected account and returns a link to configure it
   * @param userProfileId user profile id of Tournament Director
   */
  public createAccount(userProfileId: string): Observable<string> {
    const url = `/api/account/create/${userProfileId}`;
    const body = '';
    this.setLoading(true);
    return this.httpClient.post<any>(url, body)
      .pipe(
        tap(() => {
            this.setLoading(false);
          },
          () => {
            this.setLoading(false);
          }),
        map((response: any) => {
            console.log('got account link response' + JSON.stringify(response));
            return response.accountLinkUrl;
          }
        )
      );
  }

  /**
   * Using existing account it restarts where user left off and gives a new link url for configuring connected account
   * @param userProfileId user profile id of Tournament Director
   */
  public resumeAccountConfiguration(userProfileId: string): Observable<string> {
    const url = `/api/account/resume/${userProfileId}`;
    const body = '';
    this.setLoading(true);
    return this.httpClient.post<any>(url, body)
      .pipe(
        tap(() => {
            this.setLoading(false);
          },
          () => {
            this.setLoading(false);
          }),
        map((response: any) => {
            console.log('got account link response' + JSON.stringify(response));
            return response.accountLinkUrl;
          }
        )
      );
  }

  /**
   * Completes the configuration and marks it as done
   * @param userProfileId
   */
  public completeConfiguration(userProfileId: string): Observable<boolean> {
    const url = `/api/account/complete/${userProfileId}`;
    const body = '';
    this.setLoading(true);
    return this.httpClient.post<any>(url, body)
      .pipe(
        tap(() => {
            this.setLoading(false);
          },
          () => {
            this.setLoading(false);
          }),
        map((response: any) => {
            console.log('got account complete response ' + JSON.stringify(response));
            return response.accountActivated;
          }
        )
      );
  }
}

export class AccountStatus {
  public accountExists: boolean;
  public isActivated: boolean;
}
