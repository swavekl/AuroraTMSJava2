import {Directive} from '@angular/core';
import {AbstractControl, NG_VALIDATORS, ValidationErrors, Validator} from '@angular/forms';

/**
 * Checks if user specified either prize money amount or trophy or both
 */
@Directive({
    selector: '[appPrizeInfoValidator]',
    providers: [{ provide: NG_VALIDATORS, useExisting: PrizeInfoValidatorDirective, multi: true }],
    standalone: false
})
export class PrizeInfoValidatorDirective implements Validator {

  constructor() { }

  validate(control: AbstractControl): ValidationErrors | null {
    const prizeMoneyAmountCtrl = control.get('prizeMoneyAmount');
    const awardTrophyCtrl = control.get('awardTrophy');
    const prizeMoneyAmount = prizeMoneyAmountCtrl?.value;
    const awardTrophy = awardTrophyCtrl?.value;
    const isValid = awardTrophy || (prizeMoneyAmount !== '' && !isNaN(prizeMoneyAmount));
    return !isValid ? {'appPrizeInfoValidator': true} : null;
  }

}
