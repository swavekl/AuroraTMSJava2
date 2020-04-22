import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable, from } from 'rxjs';
// import { OktaAuthService } from '@okta/okta-angular';
import { environment } from '../environments/environment';
import {AuthenticationService} from './user/authentication.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(
    // private oktaAuth: OktaAuthService,
              private authenticationService: AuthenticationService) {
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return from(this.handleAccess(request, next));
  }

  private async handleAccess(request: HttpRequest<any>, next: HttpHandler): Promise<HttpEvent<any>> {
    // Only add to known domains since we don't want to send our tokens to just anyone.
    // console.log ('handleAccess for url ' + request.urlWithParams);
    const isOktaRequest = request.urlWithParams.indexOf('okta') !== -1;
    const isUsersRegistrationRequest = request.urlWithParams.indexOf('/api/users/') !== -1;
    if (!isOktaRequest && !isUsersRegistrationRequest) {
      // console.log ('Adding Bearer to request Authorization header');
      // const accessToken = await this.oktaAuth.getAccessToken();
      const accessToken = this.authenticationService.getAccessToken();

      // console.log ('adding bearer token', accessToken);
      request = request.clone({
        setHeaders: {
          Authorization: 'Bearer ' + accessToken
        }
      });
     } else {
      // console.log ('not modifying request for ' + request.urlWithParams);
    }
    return next.handle(request).toPromise();
  }
}
