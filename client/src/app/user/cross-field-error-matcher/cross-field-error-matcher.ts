import {ErrorStateMatcher} from '@angular/material/core';
import {UntypedFormControl, FormGroupDirective, NgForm} from '@angular/forms';

export class CrossFieldErrorMatcher implements ErrorStateMatcher {
  isErrorState(control: UntypedFormControl | null, form: FormGroupDirective | NgForm | null): boolean {
    return control.dirty && form.invalid;
  }
}
