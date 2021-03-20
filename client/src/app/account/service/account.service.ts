import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import {first, map} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class AccountService {

  constructor(private httpClient: HttpClient) {

  }

  public isAccountConfigured(userProfileId: string): Observable<boolean> {
    const url = `/api/account/exists/${userProfileId}`;
    console.log('url ' + url);
    return this.httpClient.get<any>(url)
      .pipe(
        map((response: any) => {
          console.log('response' + JSON.stringify(response));
            return response.accountExists;
          }
        )
      );
  }

  /**
   * Creates Stripe connected account and returns a link to configure it
   * @param userProfileId
   */
  public createAccount(userProfileId: string): Observable<string> {
    const url = `/api/account/create/${userProfileId}`;
    const body = '';
    return this.httpClient.post<any>(url, body)
      .pipe(
        map((response: any) => {
            console.log('got account link response' + response);
            return response.accountLinkUrl;
          }
        )
      );
  }

  public completeConfiguration(userProfileId: string): Observable<boolean> {
    const url = `/api/account/complete/${userProfileId}`;
    const body = '';
    return this.httpClient.post<any>(url, body)
      .pipe(
        map((response: any) => {
            console.log('got account complete response' + response);
            return response.accountActivated;
          }
        )
      );
  }
}
