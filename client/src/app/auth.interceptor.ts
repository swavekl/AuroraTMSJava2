import {Injectable} from '@angular/core';
import {HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Observable} from 'rxjs';
import {AuthenticationService} from './user/authentication.service';
import {tap} from 'rxjs/operators';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private authenticationService: AuthenticationService) {
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // for login & registration related operations we don't have the token
    // for others we require it to be authorized
    const isUsersRequest = this.authenticationService.isUsersRequest(request.urlWithParams);
    if (!isUsersRequest) {
      const accessToken = this.authenticationService.getAccessToken();
      request = request.clone({
        setHeaders: {
          Authorization: 'Bearer ' + accessToken
        }
      });
    }
    // issue request and watch response
    return next.handle(request)
      .pipe(
        tap(
          () => {
          },
          (err: any) => {
            // if we get 401 i.e. Unauthorized make an attempt to silently re-authenticate using refresh token
            if (err instanceof HttpErrorResponse) {
              if (err.status !== 401) {
                return;
              }
              this.authenticationService.loginUsingRefreshToken();
            }
          }
        )
      );
  }
}
