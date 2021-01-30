import {Injectable} from '@angular/core';
import {HttpClient, HttpErrorResponse, HttpHeaders} from '@angular/common/http';
import {catchError, map} from 'rxjs/operators';
import {environment} from '../../environments/environment';
import {Observable, throwError} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {

  // JWT token
  private accessToken: string;
  private currentUser: any;
  private isAuthenticated$: Observable<boolean>;

  constructor(private http: HttpClient) {
    this.accessToken = null;
  }

  getFullUrl(partialUrl: string) {
    // return 'https://' + environment.baseServer + partialUrl;
    return partialUrl;
  }

  /**
   * Register (Sign up a new user)
   */
  register(firstName: string, lastName: string, email: string, password: string, password2: string): Observable<boolean> {
    const requestBody = {firstName: firstName, lastName: lastName, email: email, password: password};
    return this.http.post(this.getFullUrl('/api/users/register'), requestBody)
      .pipe(
        map((response: Response) => {
          return (response.status === 200);
        })
      );
  }

  /**
   *
   */
  validateEmail(email: string, token: string) {
    const requestBody = {email: email, secondEmail: token};
    return this.http.post(this.getFullUrl('/api/users/validateEmail'), requestBody)
      .pipe(
        map((response: Response) => {
          return (response.status === 200);
        })
      );
  }

  /**
   * Login
   */
  login(username: string, password: string) {
    const requestBody = {email: username, password: password};
    this.isAuthenticated$ = this.http.post(this.getFullUrl('/api/users/login'), requestBody)
      .pipe(
        map((response: any) => {
          // console.log('logged in', response);
          // login successful if there's a jwt token in the response
          // if (response.status === 200 && response.accessToken) {
          if (response.access_token) {
            this.accessToken = response.access_token;
            this.currentUser = response;
            // store user details and jwt token in local storage to keep user logged in between page refreshes
            sessionStorage.setItem('currentUser', JSON.stringify(response));
          }
          return response;
        })
        // ,catchError (this.handleError)
      );
    return this.isAuthenticated$;
  }

  private handleError(error: HttpErrorResponse) {
    if (error.error instanceof ErrorEvent) {
      // A client-side or network error occurred. Handle it accordingly.
      console.error('An error occurred:', error.error.message);
    } else {
      // The backend returned an unsuccessful response code.
      // The response body may contain clues as to what went wrong.
      console.error(
        `Backend returned code ${error.status}, ` +
        `body was: ${error.error}`);
    }
    // Return an observable with a user-facing error message.
    return throwError(
      'Something bad happened; please try again later.');
  }
  getIsAuthenticated() {
    return this.isAuthenticated$;
  }

  /**
   * Logout
   */
  logout() {
    this.accessToken = null;
    this.currentUser = null;
    // remove user from local storage to log user out
    sessionStorage.removeItem('currentUser');
  }

  getAccessToken() {
    return this.accessToken;
  }

  getCurrentUser() {
    return this.currentUser;
  }

  /**
   * Initiates forgot password flow
   * @param email
   */
  forgotPassword(email: string): Observable<any> {
    const url = `/api/users/forgotpassword/${email}`;
    return this.http.get(url)
      .pipe(
        map((response: any) => {
          console.log ('forgotpassword response', response);
          return response;
        })
      );
  }

  /**
   * finishes reset password flow
   * @param resetPasswordToken
   * @param password
   */
  resetPassword(resetPasswordToken: string, password: string): Observable<any> {
    const url = `/api/users/resetpassword/`;
    const requestBody = {password: password, resetPasswordToken: resetPasswordToken};
    return this.http.post(url, requestBody)
      .pipe(
        map((response: any) => {
          console.log ('reset password response');
          return response;
        })
      );
  }
}
