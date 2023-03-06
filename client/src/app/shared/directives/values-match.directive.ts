import {Directive, Input} from '@angular/core';
import {AbstractControl, NG_VALIDATORS, ValidationErrors, Validator} from '@angular/forms';

/**
 * Validator for checking if two control values are the same
 */
@Directive({
  selector: '[appValuesMatch]',
  providers: [{provide: NG_VALIDATORS, useExisting: ValuesMatchDirective, multi: true}]
})
export class ValuesMatchDirective implements Validator {

  @Input('appValuesMatch')
  otherControlName: string;

  constructor() { }

  validate(control: AbstractControl): ValidationErrors | null {
    const otherCtrl = control.parent.get(this.otherControlName);
    const otherValue = otherCtrl?.value;
    const thisValue = control?.value;
    // console.log(`otherValue [${this.otherControlName}] = ${otherValue}`);
    // console.log(`thisValue [password2] = ${thisValue}`);
    const isMatching = (otherValue != null) && (thisValue != null) && (otherValue === thisValue);
    // console.log('isMatching', isMatching);
    return !isMatching ? {'appValuesMatch': true} : null;
  }
}
