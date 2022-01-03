import {Directive, Input} from '@angular/core';
import {AbstractControl, NG_VALIDATORS, ValidationErrors, Validator} from '@angular/forms';

/**
 * Validator for checking if two control values are different
 */
@Directive({
  selector: '[appValuesDifferent]',
  providers: [{provide: NG_VALIDATORS, useExisting: SelectionsDifferentDirective, multi: true}]
})
export class SelectionsDifferentDirective implements Validator {

  @Input('appValuesDifferent')
  otherControlName: string;

  constructor() { }

  validate(control: AbstractControl): ValidationErrors | null {
    const otherCtrl = control.parent.get(this.otherControlName);
    const otherValue = otherCtrl?.value;
    const thisValue = control?.value;
    const isValid = (otherValue != null) && (thisValue != null) && (otherValue !== thisValue);
    return !isValid ? {'appValuesDifferent': true} : null;
  }
}
