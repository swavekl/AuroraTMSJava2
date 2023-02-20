import {Injectable} from '@angular/core';
import {HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {BehaviorSubject, Observable, throwError} from 'rxjs';
import {AuthenticationService} from './user/authentication.service';
import {catchError, filter, finalize, switchMap, take} from 'rxjs/operators';
import {Router} from '@angular/router';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  // quiet login is when we try to get new access token using refresh token
  private quiteLoginInProgress = false;

  // signal that we got new access token
  private refreshTokenSubject: BehaviorSubject<any> = new BehaviorSubject<any>(null);

  private counter = 0;

  constructor(private authenticationService: AuthenticationService,
              private router: Router) {
  }

  /**
   * Main entry point
   * @param request
   * @param next
   */
  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

    request = this.addAuthenticationToken(request);

    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error && error.status === 401) {
          // 401 errors are most likely going to be because we have an expired token that we need to refresh.
          if (this.quiteLoginInProgress) {
            // If quiteLoginInProgress is true, we will wait until refreshTokenSubject has a non-null value
            // which means the new token is ready and we can retry the request again
            return this.refreshTokenSubject.pipe(
              filter(result => result !== null),
              take(1),
              switchMap((value: any) => {
                // console.log ('reissuing waiting request now that refresh request returned value', value);
                if (value === true) {
                    return next.handle(this.addAuthenticationToken(request));
                  } else {
                    // console.log('(1) refresh failed so ....');
                    this.router.navigate(['/ui/login']);
                    return throwError(error);
                  }
                }
              )
            );
          } else {
            this.quiteLoginInProgress = true;

            // Set the refreshTokenSubject to null so that subsequent API calls will wait until the new token has been retrieved
            this.refreshTokenSubject.next(null);
            // console.log ('attempting to refresh access token');
            return this.authenticationService.loginUsingRefreshToken().pipe(
              switchMap((success: boolean) => {
                // console.log ('refreshing access token result', success);
                this.refreshTokenSubject.next(success);
                if (success) {
                  // console.log ('issuing original data request', request.urlWithParams);
                  // retry original request
                  return next.handle(this.addAuthenticationToken(request));
                } else {
                  // refreshing access token failed - our only option is to ask user to login
                  // console.log('(2) refresh failed so ....');
                  this.router.navigate(['/ui/login']);
                  return throwError(error);
                }
              }),
              // When the call to refreshToken completes we reset the quiteLoginInProgress to false
              // for the next time the token needs to be refreshed
              finalize(() => this.quiteLoginInProgress = false)
            );
          }
        } else {
          return throwError(error);
        }
      })
    );
  }

  /**
   *
   * @param request
   * @private
   */
  private addAuthenticationToken(request: HttpRequest<any>): HttpRequest<any> {
// this.counter++;
// console.log (this.counter + ' for url ' + request.urlWithParams);
// if (this.counter > 6) {
//   this.counter = 0;
//   console.log ('faking failed request...');
//   return request;
// }
    // for login & registration related operations we don't have the token
    // for others we require it to be authorized
    const isUsersRequest = this.authenticationService.isUsersRequest(request.urlWithParams);
    if (!isUsersRequest) {
      const accessToken = this.authenticationService.getAccessToken();
      if (accessToken) {
        return request.clone({
          headers: request.headers.set('Authorization', 'Bearer ' + accessToken)
        });
      }
    }
    return request;
  }
}
