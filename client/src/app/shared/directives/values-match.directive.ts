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
  controlNames: string;

  constructor() { }

  validate(formGroup: AbstractControl): ValidationErrors | null {
    const controlNamesArray = (this.controlNames != null) ? this.controlNames.split(',') : [];
    if (controlNamesArray.length === 2) {
      const controlOneName = controlNamesArray[0].trim();
      const controlTwoName = controlNamesArray[1].trim();
      const ctrlOne = formGroup.get(controlOneName);
      const ctrlTwo = formGroup.get(controlTwoName);
      const valueOne = ctrlOne?.value;
      const valueTwo = ctrlTwo?.value;
      // console.log(`valueOne [${controlOneName}]  = ${valueOne}`);
      // console.log(`valueTwo [${controlTwoName}] = ${valueTwo}`);
      const isMatching = (valueOne != null) && (valueTwo != null) && (valueOne === valueTwo);
      // console.log('isMatching', isMatching);
      return !isMatching ? {'appValuesMatch': true} : null;
    } else {
      return {'appValuesMatch': true};
    }
  }
}
