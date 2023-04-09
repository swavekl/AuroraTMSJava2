import {AbstractControl, AsyncValidator, ValidationErrors} from '@angular/forms';
import {Observable, of} from 'rxjs';
import {catchError, map} from 'rxjs/operators';
import {AuthenticationService} from './authentication.service';
import {Injectable} from '@angular/core';

/**
 * Validator for checking if user is already registered
 */
@Injectable({
  providedIn: 'root'
})
export class LoginUniqueValidator implements AsyncValidator{

  constructor(private authenticationService: AuthenticationService) {
  }

  validate(control: AbstractControl): Observable<ValidationErrors | null> {
    const email: string = control.value;
    return this.authenticationService.isUserRegistered(email)
      .pipe(
        map(isRegistered => {
          console.log('isRegistered', isRegistered);
          return isRegistered ? {appLoginUnique: false} : null;
        }),
        catchError(() => of(null)));
  }
}
