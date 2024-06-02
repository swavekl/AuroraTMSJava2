import {Injectable} from '@angular/core';
import {HttpClient, HttpErrorResponse} from '@angular/common/http';
import {BehaviorSubject, Observable, of, Subject, throwError} from 'rxjs';
import {JWTDecoderService} from './jwtdecoder.service';
import {catchError, first, map} from 'rxjs/operators';
import {DateUtils} from '../shared/date-utils';
import {ConfirmationPopupComponent} from '../shared/confirmation-popup/confirmation-popup.component';
import {MatDialog} from '@angular/material/dialog';
import {ErrorMessagePopupData} from '../shared/error-message-dialog/error-message-popup.component';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {

  // JWT token
  private accessToken: string;
  private currentUser: any;

  // observable for notifying clients of the authentication state
  private isAuthenticated$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

  // observable for reporting login progress
  private loginStatus$: Subject<boolean> = new Subject<boolean>();

  private refreshTokenResponseStatus$: Subject<boolean> = new Subject<boolean>();

  private readonly sessionStorageKey = 'currentUser';

  private membershipExpirationDate: Date = null;

  constructor(private http: HttpClient,
              private jwtDecoderService: JWTDecoderService,
              private dialog: MatDialog) {
    this.accessToken = null;
    this.currentUser = null;
    this.checkIfTokenStillValid();
  }

  getFullUrl(partialUrl: string) {
    // return 'https://' + environment.baseServer + partialUrl;
    return partialUrl;
  }

  isUsersRequest (url: string): boolean {
    return (url.indexOf('/api/users/') !== -1);
  }

  /**
   * Register (Sign up a new user)
   */
  register(firstName: string, lastName: string, email: string, password: string, password2: string, registerByTD: boolean): Observable<string> {
    const requestBody = {firstName: firstName, lastName: lastName, email: email, password: password};
    const url: string = (registerByTD) ? '/api/users/registerbytd' : '/api/users/register';
    return this.http.post(this.getFullUrl(url), requestBody)
      .pipe(
        map((response: any) => {
            // console.log('got register response ' + JSON.stringify(response));
            if (response.status === 'SUCCESS') {
              return response.profileId;
            } else {
              return null;
            }
          },
          (error: any) => {
            this.showErrorPopup(error);
            return null;
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
   * Checks if token is still valid so they don't have to constantly login.
   * @private
   */
  private checkIfTokenStillValid(): void {
    // if they didn't explicitly log out see if we can log them in silently
    let isStillValid: boolean;
    isStillValid = false;
    const currentUserJSON = sessionStorage.getItem(this.sessionStorageKey);
    if (currentUserJSON != null) {
      const currentUser = JSON.parse(currentUserJSON);
      if (currentUser.expiresAt != null) {
        const expiresAtDate: Date = new DateUtils().convertFromString(currentUser.expiresAt);
        const now: Date = new Date();
        // console.log ('now          ', now);
        // console.log ('expiresAtDate', expiresAtDate);
        isStillValid = new DateUtils().isTimestampBefore(now, expiresAtDate);
        if (isStillValid) {
          this.currentUser = currentUser;
          this.accessToken = currentUser.access_token;
        }
      }
    }
    // console.log ('emitting isAuthenticated$', isStillValid);
    this.isAuthenticated$.next(isStillValid);
  }

  /**
   * Login
   */
  login(username: string, password: string): Observable<boolean> {
    const requestBody = {email: username, password: password};
    this.http.post(this.getFullUrl('/api/users/login'), requestBody)
      .pipe(first())
      .subscribe(
        (response: any) => {
          // console.log('login response', response);
          // login successful if there's a jwt token in the response
          this.processLoginResponse(response);
        },
        (error: any) => {
          // console.log ('got login error', error);
          this.isAuthenticated$.next(false);
          this.loginStatus$.next(false);
        });
    return this.loginStatus$;
  }

  /**
   * Attempts to silently relogin user without redirecting to login screen
   */
  loginUsingRefreshToken(): Observable<boolean> {
    const refreshToken = this.currentUser?.refresh_token;
    const email = this.currentUser?.profile?.email;
    if (refreshToken != null && email != null) {
      const requestBody = { refreshToken: refreshToken, email: email };
      this.http.post(this.getFullUrl('/api/users/loginquiet'), requestBody)
        .pipe(first())
        .subscribe(
          (response: any) => {
            // console.log ('got good response from refresh.. processing');
            this.processLoginResponse(response);
            this.refreshTokenResponseStatus$.next(true);
        },
          (error: any) => {
            // console.log ('got error from refresh token request');
            this.isAuthenticated$.next(false);
            this.loginStatus$.next(false);
            this.refreshTokenResponseStatus$.next(false);
          });
    } else {
      this.isAuthenticated$.next(false);
      this.loginStatus$.next(false);
      this.refreshTokenResponseStatus$.next(false);
    }
    return this.refreshTokenResponseStatus$;
  }

  /**
   * Common processing for login and refresh token response
   * @param response
   * @private
   */
  private processLoginResponse(response: any) {
    if (response.access_token) {
      this.accessToken = response.access_token;
      this.currentUser = response;
      const expires_in = response.expires_in || 0;
      const expiresAt = new DateUtils().getExpiresAt(expires_in);
      // console.log('setting expiresAt', expiresAt);
      const withExpirationDate = {
        ...response,
        expiresAt: expiresAt
      };

      // store user details and jwt token in local storage to keep user logged in between page refreshes
      sessionStorage.setItem(this.sessionStorageKey, JSON.stringify(withExpirationDate));
      this.isAuthenticated$.next(true);
      this.loginStatus$.next(true);
    } else {
      this.isAuthenticated$.next(false);
      this.loginStatus$.next(false);
    }
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

  getIsAuthenticated(): Observable<boolean> {
    return this.isAuthenticated$.asObservable();
  }

  /**
   * Logout
   */
  logout() {
    this.accessToken = null;
    this.currentUser = null;
    // remove user from local storage to log user out
    sessionStorage.removeItem(this.sessionStorageKey);
    this.isAuthenticated$.next(false);
  }

  getAccessToken() {
    return this.accessToken;
  }

  getCurrentUser() {
    return this.currentUser;
  }

  getCurrentUserFirstName() {
    return this.currentUser?.profile?.firstName;
  }

  getCurrentUserLastName() {
    return this.currentUser?.profile?.lastName;
  }

  getCurrentUserState() {
    return this.currentUser?.profile?.state;
  }

  getCurrentUserCountry() {
    return this.currentUser?.profile?.countryCode;
  }

  getCurrentUserMembershipId() {
    return this.currentUser?.profile?.membershipId;
  }

  setCurrentUserMembershipId(membershipId: number) {
    if (this.currentUser?.profile != null) {
      this.currentUser.profile = {
        ...this.currentUser.profile,
        membershipId: membershipId
      };
    }
  }

  getCurrentUserMembershipExpiration() {
    return this.membershipExpirationDate;
  }

  setCurrentUserMembershipExpiration(membershipExpirationDate: Date) {
    this.membershipExpirationDate = membershipExpirationDate;
  }

  getCurrentUserBirthDate() {
    return this.currentUser?.profile?.birthdate;
  }

  setCurrentUserBirthDate(birthdate: Date) {
    if (this.currentUser?.profile != null) {
      this.currentUser.profile = {
        ...this.currentUser.profile,
        birthdate: birthdate
      };
    }
  }

  getCurrentUserProfileId() {
    return this.currentUser?.id;
  }

  isProfileComplete(): boolean {
    let isComplete: boolean = false;
    if (this.currentUser != null && this.currentUser.profile != null) {
      isComplete = this.currentUser.profile.isProfileComplete === true;
    }
    // console.log('isProfileComplete', isComplete);
    return isComplete;
  }


  makeProfileComplete() {
    if (this.currentUser != null && this.currentUser.profile != null && !this.currentUser.profile.isProfileComplete) {
      this.currentUser.profile = {
        ...this.currentUser.profile,
        isProfileComplete: true
      };
    }
  }

  public getCurrentUserRoles() {
    const decodedToken = this.jwtDecoderService.decode(this.getAccessToken());
    const roles = decodedToken?.groups;
    // if (roles) {
    //   console.log('user roles' + JSON.stringify(roles));
    // }
    return (roles) ? roles : [];
  }

  /**
   * Checks if user has a role required for this route
   * @param routeRoles
   * @private
   */
  public hasCurrentUserRole(routeRoles: string []): boolean {
    const userRoles = this.getCurrentUserRoles();
    let hasRole = false;
    if (routeRoles != null) {
      for (let i = 0; i < userRoles.length; i++) {
        const userRole = userRoles[i];
        hasRole = (routeRoles.indexOf(userRole) !== -1);
        if (hasRole) {
          break;
        }
      }
    } else {
      hasRole = true; // no route roles means it is unprotected so allow
    }
    return hasRole;
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
          // console.log('forgotpassword response', response);
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
          // console.log('reset password response');
          return response;
        })
      );
  }

  private showErrorPopup(error: any) {
    const errorMessageData: ErrorMessagePopupData = {
      errorMessage: error,
      title: null
    };
    const config = {
      width: '450px', height: '250px', data: errorMessageData
    };
    const dialogRef = this.dialog.open(ConfirmationPopupComponent, config);
    dialogRef.afterClosed().subscribe(result => {
      console.log('closed error message popup');
    });
  }

  public isUserRegistered(email: string): Observable<boolean> {
    const partialUrl = `/api/users/isregistered/${email}`;
    return this.http.get(this.getFullUrl(partialUrl))
      .pipe(
        map((response: any) => {
          return (response?.userRegistered === true);
        }),
        catchError((err, caught) => {
            return of(false);
        })
      );

  }

  isLiveScoreRole() {
    return this.hasCurrentUserRole(['Monitors', 'DigitalScoreBoards']);
  }
}
