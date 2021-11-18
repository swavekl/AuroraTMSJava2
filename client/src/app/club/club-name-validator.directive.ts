import {Directive, Input} from '@angular/core';
import {AbstractControl, NG_VALIDATORS, ValidationErrors, Validator} from '@angular/forms';
import {Club} from './model/club.model';
import {createGlobalSettings} from '@angular/cli/utilities/config';

/**
 * Validator for checking if club names are valid i.e. among the clubs loaded from server
 */
@Directive({
  selector: '[appClubName]',
  providers: [{provide: NG_VALIDATORS, useExisting: ClubNameValidatorDirective, multi: true}]

})
export class ClubNameValidatorDirective implements Validator {
  @Input('appClubName')
  clubs: Club[];

  constructor() {
  }

  validate(control: AbstractControl): ValidationErrors {
    const clubNameToValidate = control?.value;
    let isValid = false;
    if (this.clubs && this.clubs.length > 0) {
      for (const club of this.clubs) {
        if (club.clubName === clubNameToValidate) {
          isValid = true;
          break;
        }
      }
    }
    return !isValid ? {'appClubName': true} : null;
  }
}
