import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {map} from 'rxjs/operators';
import {environment} from '../../environments/environment';
import {Observable} from 'rxjs';

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
    return 'https://' + environment.baseServer + partialUrl;
  }

  /**
   * Register (Sign up a new user)
   */
  register(firstName: string, lastName: string, email: string, password: string, password2: string) {
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
          console.log('logged in', response);
          // login successful if there's a jwt token in the response
          // if (response.status === 200 && response.accessToken) {
          if (response.accessToken) {
            this.accessToken = response.accessToken;
            this.currentUser = response;
            // store user details and jwt token in local storage to keep user logged in between page refreshes
            sessionStorage.setItem('currentUser', JSON.stringify(response));
          }
          return response;
        })
      );
    return this.isAuthenticated$;
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
}
