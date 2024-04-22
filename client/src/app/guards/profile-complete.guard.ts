import {ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot} from '@angular/router';
import {AuthenticationService} from '../user/authentication.service';
import {inject, Injectable} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ProfileCompleteService {
  constructor(
    private router: Router,
    private authenticationService: AuthenticationService
  ) {
  }

  canActivate(route: ActivatedRouteSnapshot,
              state: RouterStateSnapshot): boolean {
    const currentUser = this.authenticationService.getCurrentUser();
    if (currentUser) {
      if (!this.authenticationService.isProfileComplete()) {
        // console.log('User profile is not complete ', route);
        this.router.navigate(['/ui/home'], {queryParams: {returnUrl: state.url}});
        return false;
      } else {
        return true;
      }
    }
    return true;
  }
}

export const ProfileCompleteGuard: CanActivateFn = (next: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean => {
  return inject(ProfileCompleteService).canActivate(next, state);
}

