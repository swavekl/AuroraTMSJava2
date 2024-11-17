import {Injectable, inject} from '@angular/core';
import {Router, ActivatedRouteSnapshot, RouterStateSnapshot, CanActivateFn, UrlSegment} from '@angular/router';
import {AuthenticationService} from '../user/authentication.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuardService  {
  constructor(
    private router: Router,
    private authenticationService: AuthenticationService
  ) {
  }

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): boolean {
    const currentUser = this.authenticationService.getCurrentUser();
    if (currentUser) {
      // check if route is restricted by role
      if (!this.authenticationService.hasCurrentUserRole(route.data?.roles)) {
        // console.log('User is not in role to access this page ', route);
        // role not authorised so don't navigate away from current page
        return false;
      }

      // authorised so return true
      // console.log('User is in role to access this page ', route);
      return true;
    } else {
      // not logged in so redirect to login page with the return url
      // unless it is one of the public routes
      if (this.isPublicRoute(route.url)) {
        return true;
      } else {
        this.router.navigate(['/ui/login'], {queryParams: {returnUrl: state.url}});
        return false;
      }
    }
  }

  private isPublicRoute(urlSegments: UrlSegment[]): boolean {
    if (urlSegments != null) {
      const publicPlayerList = urlSegments.filter((segment: UrlSegment) => {
        return 'playerlist' === segment.path;
      });
      return (publicPlayerList.length > 0);
    } else {
      return false;
    }
  }
}

export const AuthGuard: CanActivateFn = (next: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean => {
  return inject(AuthGuardService).canActivate(next, state);
}
