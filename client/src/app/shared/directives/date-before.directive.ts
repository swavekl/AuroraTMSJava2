import {Directive, Input} from '@angular/core';
import {AbstractControl, NG_VALIDATORS, ValidationErrors, Validator} from '@angular/forms';

/**
 * Directive for checking if date in current control is before date of another control specified by appDateBefore
 */
@Directive({
    selector: '[appDateBefore]',
    providers: [{ provide: NG_VALIDATORS, useExisting: DateBeforeDirective, multi: true }],
    standalone: false
})
export class DateBeforeDirective implements Validator {
  @Input('appDateBefore')
  otherDateCtrlName: string;

  validate(control: AbstractControl): ValidationErrors {
    const thisDate = control?.value;
    const otherDateCtrl = control.parent.get(this.otherDateCtrlName);
    const otherDate = otherDateCtrl?.value;
    const isValid: boolean = thisDate instanceof Date && otherDate instanceof Date && thisDate.getTime() < otherDate.getTime();
    return !isValid ? {'appDateBefore': true} : null;
  }
}
