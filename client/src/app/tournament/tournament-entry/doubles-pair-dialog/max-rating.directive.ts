import {Directive} from '@angular/core';
import {AbstractControl, NG_VALIDATORS, ValidationErrors, Validator} from '@angular/forms';
import {DoublesPairingInfo} from './doubles-pair-dialog.component';

@Directive({
    selector: '[appMaxRating]',
    providers: [{ provide: NG_VALIDATORS, useExisting: MaxRatingDirective, multi: true }],
    standalone: false
})
export class MaxRatingDirective implements Validator {

  constructor() { }

  validate(control: AbstractControl): ValidationErrors | null {
    const playerACtrl = control.get('playerA');
    const playerBCtrl = control.get('playerB');
    const playerAEventId = playerACtrl?.value;
    const playerBEventId = playerBCtrl?.value;
    const maxRatingCtrl = control.get('eventMaxRating');
    const eventMaxRating = maxRatingCtrl?.value;
    const doublesParingInfosCtrl = control.get('doublesPairingInfos');
    const doublesParingInfos: DoublesPairingInfo [] = doublesParingInfosCtrl?.value;
    let combinedRating = 0;
    let isValid = true;
    if (doublesParingInfos && playerAEventId && playerBCtrl && eventMaxRating) {
      doublesParingInfos.forEach((doublesPairingInfo: DoublesPairingInfo) => {
        if (doublesPairingInfo.eventEntryId === playerAEventId ||
          doublesPairingInfo.eventEntryId === playerBEventId) {
          combinedRating += doublesPairingInfo.playerRating;
        }
      });
      isValid = (eventMaxRating > 0) ? (combinedRating < eventMaxRating) : true;
    }
    return !isValid ? {'appMaxRating': true} : null;
  }

}
