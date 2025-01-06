import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {DrawItem} from '../../draws-common/model/draw-item.model';
import {TournamentEvent} from '../../../tournament/tournament-config/tournament-event.model';
import {TournamentEntryInfoService} from '../../../tournament/service/tournament-entry-info.service';
import {first} from 'rxjs/operators';
import {TournamentEntryInfo} from '../../../tournament/model/tournament-entry-info.model';
import {DrawType} from '../../draws-common/model/draw-type.enum';
import {GenderRestriction} from '../../../tournament/tournament-config/model/gender-restriction.enum';
import {AgeRestrictionType} from '../../../tournament/tournament-config/model/age-restriction-type.enum';
import {CheckCashPaymentDialogService} from '../../../account/service/check-cash-payment-dialog.service';
import {PaymentRequest} from '../../../account/model/payment-request.model';
import {PaymentRefundFor} from '../../../account/model/payment-refund-for.enum';
import {PaymentDialogData} from '../../../account/payment-dialog/payment-dialog-data';
import {CallbackData} from '../../../account/model/callback-data';
import {ProfileService} from '../../../profile/profile.service';
import {Profile} from '../../../profile/profile';
import {ErrorMessagePopupService} from '../../../shared/error-message-dialog/error-message-popup.service';

@Component({
  selector: 'app-replace-player-popup',
  templateUrl: './replace-player-popup.component.html',
  styleUrl: './replace-player-popup.component.scss'
})
export class ReplacePlayerPopupComponent {
  drawGroups: number [] = [];
  selectedGroup: number = 1;
  playerToRemove: string;
  playerToAdd: string;
  playerToRemoveEntryId: number;
  playerToAddEntryId: number;
  groupPlayers: PlayerInfo [] = [];
  filteredPlayers: PlayerInfo [] = [];
  tournamentEvent: TournamentEvent;
  drawItems: DrawItem [];
  isLoading: boolean;

  // keep it since it has entry id needed for payment
  private entryInfos: TournamentEntryInfo [] = [];

  // tournament name needed for recording payment
  private tournamentName: string;

  // indicates if payment was recorded for this entry
  paymentRecorded: boolean;

  constructor(public dialogRef: MatDialogRef<ReplacePlayerPopupComponent>,
              @Inject(MAT_DIALOG_DATA) public data: ReplacePlayerPopupData,
              private tournamentEntryInfoService: TournamentEntryInfoService,
              private checkCashPaymentDialogService: CheckCashPaymentDialogService,
              private profileService: ProfileService,
              private errorMessagePopupService: ErrorMessagePopupService) {
    this.tournamentEvent = data.tournamentEvent;
    this.drawItems = data.drawItems;
    this.tournamentName = data.tournamentName;
    this.paymentRecorded = false;
    const drawItems = data.drawItems || [];
    let maxGroups = 0;
    for (const drawItem of drawItems) {
      maxGroups = Math.max(drawItem.groupNum, maxGroups);
    }
    this.drawGroups = new Array(maxGroups);
    this.fetchAllPlayers(this.tournamentEvent.tournamentFk);
    this.onGroupChange({value: 1});
  }

  onCancel() {
    this.dialogRef.close({action: 'cancel', request: null});
  }

  onReplace() {
    const playerToAdd = this.playerToAdd[0];
    const playerToRemove = this.playerToRemove[0];
    const request: ReplacePlayerRequest = {
      playerToAdd: playerToAdd,
      playerToRemove: playerToRemove,
      tournamentEventId: this.tournamentEvent.id,
      playerToAddEntryId: this.playerToAddEntryId,
      playerToRemoveEntryId: this.playerToRemoveEntryId
    };
    this.dialogRef.close({action: 'ok', request: request});
  }

  onSelectPlayerToRemove(profileId: string) {
    const entryInfo = this.findEntryInfo(profileId);
    this.playerToRemoveEntryId = entryInfo.entryId;
  }

  onSelectPlayerToAdd(profileId: any) {
    if (this.playerToAdd != profileId) {
      this.paymentRecorded = false;
    }
    const entryInfo = this.findEntryInfo(profileId);
    this.playerToAddEntryId = entryInfo.entryId;
  }

  onGroupChange($event: any) {
    const groupNum = $event.value;
    let groupPlayers: PlayerInfo[] = this.drawItems.filter((drawItem: DrawItem): boolean => {
      return (drawItem.groupNum === groupNum) && drawItem.drawType === DrawType.ROUND_ROBIN
        && drawItem.playerName != null && drawItem.playerId != null;
    }).map((drawItem: DrawItem): PlayerInfo => {
      return {profileId: drawItem.playerId, playerName: drawItem.playerName, rating: drawItem.rating, placeInGroup: drawItem.placeInGroup};
    });
    if (this.tournamentEvent.doubles) {
      let doublesPlayers: any [] = [];
      for (const doublesTeamPlayers of groupPlayers) {
        const playerIds: string[] = doublesTeamPlayers.profileId.split(';');
        const playerNames: string[] = doublesTeamPlayers.playerName.split(' / ');
        const placeInGroup = doublesTeamPlayers.placeInGroup;
        if (playerIds?.length === 2 && playerNames?.length === 2) {
          doublesPlayers.push(this.findPlayerInfo(playerIds[0], playerNames[0], placeInGroup));
          doublesPlayers.push(this.findPlayerInfo(playerIds[1], playerNames[1], placeInGroup));
        }
      }
      groupPlayers = doublesPlayers;
    }

    this.groupPlayers = groupPlayers;
    // this.groupPlayers = groupPlayers.sort((pi1: PlayerInfo, pi2: PlayerInfo) => {
    //   return pi1.playerName.localeCompare(pi2.playerName);
    // });
  }

  private findPlayerInfo(profileId: string, playerName: string, placeInGroup: number) {
    let playerInfo: PlayerInfo = {profileId: profileId, playerName: playerName, rating: 0, placeInGroup: placeInGroup};
    // console.log('Finding player rating in doubles', playerName);
    if (this.filteredPlayers != null) {
      const foundPlayers: PlayerInfo[] = this.filteredPlayers.filter((pi: PlayerInfo): boolean => {
        return pi.profileId === profileId;
      });
      if (foundPlayers.length > 0) {
        playerInfo = {...foundPlayers[0], placeInGroup: placeInGroup};
      }
    }
    return playerInfo;
  }

  /**
   *
   * @param tournamentId
   * @private
   */
  private fetchAllPlayers(tournamentId: number) {
    this.isLoading = true;
    this.tournamentEntryInfoService.getAll(tournamentId)
      .pipe(first())
      .subscribe({
        next: (entryInfos: TournamentEntryInfo[]) => {
          let filteredEntryInfos = this.filterPlayerForEvent(entryInfos);
          this.entryInfos = filteredEntryInfos;
          const filteredPlayers = filteredEntryInfos.map((entryInfo: TournamentEntryInfo) => {
            const playerInfo: PlayerInfo = {
              playerName: `${entryInfo.lastName}, ${entryInfo.firstName}`,
              profileId: entryInfo.profileId,
              rating: entryInfo.seedRating,
              placeInGroup: 0
            };
            return playerInfo;
          });

          this.filteredPlayers = filteredPlayers.sort((pi1: PlayerInfo, pi2: PlayerInfo) => {
            return pi1.playerName.localeCompare(pi2.playerName);
          });

          // get individual ratings for doubles players - we only have combined in the draw item
          if (this.tournamentEvent.doubles) {
            this.onGroupChange({value: this.selectedGroup})
          }
        }, complete: () => {
          this.isLoading = false;
        }
      });
  }

  /**
   * Removes ineligible players from the list of infos
   * @param entryInfos
   * @private
   */
  private filterPlayerForEvent(entryInfos: TournamentEntryInfo[]) {
    let filteredEntryInfos = entryInfos;
    if (!this.tournamentEvent?.doubles) {
      if (this.tournamentEvent.maxPlayerRating > 0) {
        filteredEntryInfos = filteredEntryInfos.filter((entryInfo: TournamentEntryInfo) => {
          return entryInfo.eligibilityRating < this.tournamentEvent.maxPlayerRating;
        });
      }

      if (this.tournamentEvent.genderRestriction !== GenderRestriction.NONE) {
        const genderToAccept: string = this.tournamentEvent.genderRestriction == GenderRestriction.FEMALE ? 'F' : 'M';
        filteredEntryInfos = filteredEntryInfos.filter((entryInfo: TournamentEntryInfo) => {
            return entryInfo.gender == genderToAccept;
        });
      }

      if (this.tournamentEvent.ageRestrictionType !== AgeRestrictionType.NONE) {
        // todo - get age and filter out
      }
    }
    return filteredEntryInfos;
  }

  onRecordPayment() {
    this.profileService.getProfile(this.playerToAdd)
      .pipe(first())
      .subscribe({
        next: (profile: Profile) => {
          this.recordPaymentInternal(profile);
        },
        error: (error: any) => {
          const message = 'Error getting profile ' + this.playerToAdd + ' ' + error;
          this.errorMessagePopupService.showError(message);
        },
        complete: () => {}
      });
  }


  onPaymentSuccessful(scope: any) {
    const thisPopup: ReplacePlayerPopupComponent = scope;
    thisPopup.paymentRecorded = true;
  }

  onPaymentCanceled(scope: any) {
    const thisPopup: ReplacePlayerPopupComponent = scope;
    thisPopup.paymentRecorded = true;
  }

  private recordPaymentInternal(profile: Profile) {
    const entryInfo = this.findEntryInfo(this.playerToAdd);
    const balanceInPlayerCurrency = this.tournamentEvent.feeAdult;
    const balanceInTournamentCurrency = this.tournamentEvent.feeAdult;
    const amount: number = balanceInPlayerCurrency * 100;
    const amountInAccountCurrency: number = balanceInTournamentCurrency * 100;
    const fullName = profile.firstName + ' ' + profile.lastName;
    const postalCode = profile.zipCode;
    const email = profile.email;
    const tournamentName = this.tournamentName;
    const paymentRequest: PaymentRequest = {
      paymentRefundFor: PaymentRefundFor.TOURNAMENT_ENTRY,
      accountItemId: this.tournamentEvent.tournamentFk,
      transactionItemId: entryInfo.entryId,
      amount: amount,
      currencyCode: 'USD',  // todo
      amountInAccountCurrency: amountInAccountCurrency,
      statementDescriptor: tournamentName,
      fullName: fullName,
      postalCode: postalCode,
      receiptEmail: email,
    };

    const paymentDialogData: PaymentDialogData = {
      paymentRequest: paymentRequest,
      stripeInstance: null
    };

    const callbackData: CallbackData = {
      successCallbackFn: this.onPaymentSuccessful,
      cancelCallbackFn: this.onPaymentCanceled,
      callbackScope: this
    };
    // if (payByCreditCard == true) {
    //   this.paymentDialogService.showPaymentDialog(paymentDialogData, callbackData);
    // } else {
    this.checkCashPaymentDialogService.showPaymentDialog(paymentDialogData, callbackData);
    // }

  }

  private findEntryInfo (profileId: string): TournamentEntryInfo {
    let entryToReturn  = null;
    if (this.entryInfos != null) {
      const foundEI = this.entryInfos.filter((entryInfo: TournamentEntryInfo) => {
        return entryInfo.profileId == profileId;
      });
      if (foundEI?.length === 1) {
        entryToReturn = foundEI[0];
      }
    }
    return entryToReturn;
  }}

export interface ReplacePlayerPopupData {
  drawItems: DrawItem[];
  tournamentEvent: TournamentEvent;
  tournamentName: string;
}

export interface ReplacePlayerRequest {
  playerToRemove: string;
  playerToAdd: string;
  tournamentEventId: number;
  playerToAddEntryId: number,
  playerToRemoveEntryId: number
}

export interface PlayerInfo {
  placeInGroup: number;
  playerName: string;
  profileId: string;
  rating: number;
}
