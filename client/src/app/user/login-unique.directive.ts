import {Directive, forwardRef} from '@angular/core';
import {AbstractControl, AsyncValidator, NG_ASYNC_VALIDATORS, ValidationErrors} from '@angular/forms';
import {Observable} from 'rxjs';
import {LoginUniqueValidator} from './login-unique-validator';

/**
 * Directive for adding async validator to template driven forms
 */
@Directive({
    selector: '[appLoginUnique]',
    providers: [
        {
            provide: NG_ASYNC_VALIDATORS,
            useExisting: forwardRef(() => LoginUniqueDirective),
            multi: true
        }
    ],
    standalone: false
})
export class LoginUniqueDirective implements AsyncValidator {

  constructor(private loginUniqueValidator: LoginUniqueValidator) {
  }

  validate(control: AbstractControl): Observable<ValidationErrors | null> {
    return this.loginUniqueValidator.validate(control);
  }

}
