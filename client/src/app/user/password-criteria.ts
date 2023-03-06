import {AbstractControl, ValidationErrors, ValidatorFn} from '@angular/forms';

export class PasswordCriteria {
  public readonly passwordPattern: RegExp =
    /^(?=[a-zA-Z0-9!$^*-?#@~&%+]{8,}$)(?=.*?[a-z])(?=.*?[A-Z])(?=.*?[0-9])(?=.*?[!$^*-?#@~&%+]).*/;
  public readonly passwordRequirements: string =
    'Password must contain at least one upper and lower case letter, digit, special character ~!@#$%^&*-+? and be at least 8 characters long';

  // public passwordCheck(): ValidatorFn {
  //   return (c: AbstractControl): ValidationErrors | null => {
  //     if (c.parent) {
  //
  //       let password = c.parent.get('password');
  //       let confirmPassword = c.parent.get('confirmPassword');
  //
  //       return password.value !== confirmPassword.value? { passwordMismatch: true }: null;
  //     }
  //     return null;
  //   };
  // }
}
