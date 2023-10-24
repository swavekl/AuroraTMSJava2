import {AbstractControl, ValidationErrors, ValidatorFn} from '@angular/forms';

export class PasswordCriteria {
  public readonly passwordPattern: RegExp =
    /(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[~!@#\$%\^&\*\-+\?]).{8,}/;
  public readonly passwordRequirements: string =
    'Password requirements: at least 8 characters, a lowercase letter, an uppercase letter, a number, a symbol ~!@#$%^&*-+? and no parts of your email.';
  // 'Password must contain at least one upper and lower case letter, digit, special character ~!@#$%^&*-+? and be at least 8 characters long';
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
