import { Directive } from '@angular/core';
import { AbstractControl, NG_VALIDATORS, ValidationErrors, Validator, NgForm } from '@angular/forms';

@Directive({
  selector: '[appPrizeInfoValidator]',
  providers: [{ provide: NG_VALIDATORS, useExisting: PrizeInfoValidatorDirective, multi: true }],
  standalone: false
})
export class PrizeInfoValidatorDirective implements Validator {

  constructor() { }

  validate(control: AbstractControl): ValidationErrors | null {
    // Cast control to NgForm to access the underlying FormGroup (.form)
    const form: NgForm = (control as unknown as NgForm);
    if (form == null) {
      return null;
    }

    const controls = form.controls;
    if (controls == null) {
      return null;
    }

    // Now you can safely get the controls
    const prizeMoneyAmountCtrl = controls['prizeMoneyAmount'];
    const awardTypeCtrl = controls['awardType'];
    const otherAwardTypeCtrl = controls['otherAwardType'];

    // Add a check to ensure controls exist before validating
    if (!prizeMoneyAmountCtrl || !awardTypeCtrl) {
      return null;
    }

    const prizeMoneyAmount = prizeMoneyAmountCtrl?.value;
    const awardType = awardTypeCtrl?.value;
    const otherAwardType = otherAwardTypeCtrl?.value;

    // Your logic (updated to check for the correct awardType value)
    const isTrophySelected = awardType === 'Trophy';
    const isMedalSelected = awardType === 'Medal';
    const isOtherSelected = awardType === 'Other' && otherAwardType !== null;
    const isAnyAwardSelected = isTrophySelected || isMedalSelected || isOtherSelected;

    const hasMoney = prizeMoneyAmount !== '' && prizeMoneyAmount !== null && !isNaN(prizeMoneyAmount);
    const isValid = isAnyAwardSelected || hasMoney;

    return !isValid ? { 'appPrizeInfoValidator': true } : null;
  }
}
