import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {DrawItem} from '../../draws-common/model/draw-item.model';
import {TournamentEvent} from '../../../tournament/tournament-config/tournament-event.model';
import {TournamentEntryInfoService} from '../../../tournament/service/tournament-entry-info.service';
import {first} from 'rxjs/operators';
import {TournamentEntryInfo} from '../../../tournament/model/tournament-entry-info.model';
import {DrawType} from '../../draws-common/model/draw-type.enum';
import {PaymentDialogService} from '../../../account/service/payment-dialog.service';
import {CheckCashPaymentDialogService} from '../../../account/service/check-cash-payment-dialog.service';
import {PaymentRequest} from '../../../account/model/payment-request.model';
import {PaymentRefundFor} from '../../../account/model/payment-refund-for.enum';
import {PaymentDialogData} from '../../../account/payment-dialog/payment-dialog-data';
import {CallbackData} from '../../../account/model/callback-data';
import {ProfileService} from '../../../profile/profile.service';
import {Profile} from '../../../profile/profile';
import {ErrorMessagePopupService} from '../../../shared/error-message-dialog/error-message-popup.service';
import {ConflictType} from '../../draws-common/model/conflict-type.enum';

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

  // indicates if payment was completed for this entry
  paymentCompleted: boolean;

  // draw item id which needs to be replaced
  private drawItemToRemove: DrawItem;

  private readonly PLAYER_A_EMPTY = '(empty) A';
  private readonly PLAYER_B_EMPTY = '(empty) B';

  private readonly PLAYER_A_SIDE = 'A';
  private readonly PLAYER_B_SIDE = 'B';

  // in doubles events 'A' or 'B', in singles events null
  private playerSideToRemove: string;

  // type of draw we will be modifying
  private drawType: DrawType;

  // todo - get it from the tournament country
  protected tournamentCurrency: string = 'USD';

  // to be determined player profile id same as matches/match.model.ts
  public readonly TBD_PROFILE_ID = 'TBD';

  // for single elimination first round players infos
  firstRoundPlayers: PlayerInfo[];

  constructor(public dialogRef: MatDialogRef<ReplacePlayerPopupComponent>,
              @Inject(MAT_DIALOG_DATA) public data: ReplacePlayerPopupData,
              private tournamentEntryInfoService: TournamentEntryInfoService,
              private paymentDialogService: PaymentDialogService,
              private checkCashPaymentDialogService: CheckCashPaymentDialogService,
              private profileService: ProfileService,
              private errorMessagePopupService: ErrorMessagePopupService) {
    this.tournamentEvent = data.tournamentEvent;
    this.tournamentName = data.tournamentName;
    this.drawType = data.drawType;

    // get only draw items of given type
    const drawItems = data.drawItems || [];

    // find the first round number of single elimination type of event
    // for round robin it is 0
    let maxRound = 0;
    if (this.tournamentEvent.singleElimination) {
      maxRound = Math.max(...drawItems.map(drawItem => drawItem.round), 0);
    }

    this.drawItems = drawItems.filter((drawItem: DrawItem) => {
        return drawItem.drawType === this.drawType;
    }).filter((drawItem: DrawItem) => {
        return drawItem.round === maxRound;
    });

    if (!this.tournamentEvent.singleElimination) {
      // find max group number
      let maxGroups = 0;
      this.drawItems.forEach((drawItem: DrawItem) => {
        maxGroups = Math.max(drawItem.groupNum, maxGroups);
      });
      this.drawGroups = new Array(maxGroups);
    } else {
      this.drawGroups = [];
    }

    this.paymentCompleted = false;
    this.fetchReplacementPlayers(this.tournamentEvent.id);
    if (this.drawType === DrawType.ROUND_ROBIN) {
      this.onGroupChange({value: 1});
    } else {
      this.getFirstRoundPlayerInfos();
    }
  }

  onCancel() {
    this.dialogRef.close({action: 'cancel', request: null});
  }

  onReplace() {
    const request: ReplacePlayerRequest = {
      drawItem: this.drawItemToRemove,
      playerToAddEntryId: this.playerToAddEntryId,
      playerSideToRemove: this.playerSideToRemove
    };
    this.dialogRef.close({action: 'ok', request: request});
  }

  onSelectPlayerToRemove(playerInfo: PlayerInfo) {
    this.drawItemToRemove = this.findDrawItem(playerInfo);
    this.playerSideToRemove = playerInfo.playerSide;
    // console.log('this.drawItemToRemove', this.drawItemToRemove);
  }

  onSelectPlayerToAdd(profileId: any) {
    const currentProfileId = this.getPlayerToAddProfileId();
    if (currentProfileId != profileId) {
      this.paymentCompleted = false;
    }
    const entryInfo = this.findEntryInfo(profileId);
    this.playerToAddEntryId = entryInfo.entryId;

    // todo - finish eligibility rating check
    // if (this.tournamentEvent.doubles && this.tournamentEvent.maxPlayerRating) {
    //   const maxTeamRating = this.tournamentEvent.maxPlayerRating;
    //   // check if they will exceed
    //   let sideAPlayerRating = 0;
    //   let sideBPlayerRating = 0;
    //   const playerProfiles = (this.drawItemToRemove?.playerId != null) ? this.drawItemToRemove.playerId.split(';') : [];
    //   const playerNames = (this.drawItemToRemove?.playerName != null) ? this.drawItemToRemove.playerName.split('/') : [];
    //   if (this.playerSideToRemove === this.PLAYER_A_SIDE) {
    //     sideAPlayerRating = entryInfo.eligibilityRating;
    //     if (playerProfiles.length > 1 && playerProfiles[1] != this.TBD_PROFILE_ID) {
    //       const sideBPlayerProfile = playerProfiles[1];
    //       const sideBPlayerName = (playerNames.length > 1) ? playerNames[1] : this.PLAYER_B_EMPTY;
    //       const sideBPlayerInfo = this.findPlayerInfo(sideBPlayerProfile, sideBPlayerName, this.drawItemToRemove.placeInGroup, this.PLAYER_B_SIDE);
    //       sideBPlayerRating = sideBPlayerInfo.rating;
    //     }
    //   } else {
    //     if (playerProfiles.length > 0 && playerProfiles[0] != this.TBD_PROFILE_ID) {
    //       const sideAPlayerProfile = playerProfiles[0];
    //       const sideAPlayerName = (playerNames.length > 0) ? playerNames[0] : this.PLAYER_A_EMPTY;
    //       const sideAPlayerInfo = this.findPlayerInfo(sideAPlayerProfile, sideAPlayerName, this.drawItemToRemove.placeInGroup, this.PLAYER_A_SIDE);
    //       sideAPlayerRating = sideAPlayerInfo.rating;
    //     }
    //     sideBPlayerRating = entryInfo.eligibilityRating;
    //   }
    //   if (sideAPlayerRating + sideBPlayerRating >= this.tournamentEvent.maxPlayerRating) {
    //     console.log('Max rating exceeded' + (sideAPlayerRating + sideBPlayerRating) + ', max is: ' + this.tournamentEvent.maxPlayerRating);
    //   }
    // }
  }

  /**
   * Gets player infos for currently selected group
   * @param $event
   */
  onGroupChange($event: any) {
    const groupNum = $event.value;
    let groupPlayers: PlayerInfo[] = this.drawItems.filter((drawItem: DrawItem): boolean => {
      return (drawItem.groupNum === groupNum) && drawItem.playerName != null && drawItem.playerId != null;
    }).map((drawItem: DrawItem): PlayerInfo => {
      return {profileId: drawItem.playerId, playerName: drawItem.playerName, rating: drawItem.rating, placeInGroup: drawItem.placeInGroup, byeNum: 0,
        seSeedNumber: 0, singleEliminationLineNum: 0, playerSide: null};
    });

    if (this.tournamentEvent.doubles) {
      let doublesPlayers: any [] = [];
      for (const doublesTeamPlayers of groupPlayers) {
        const playerIds: string[] = doublesTeamPlayers.profileId.split(';');
        const playerNames: string[] = doublesTeamPlayers.playerName.split(' / ');
        const placeInGroup = doublesTeamPlayers.placeInGroup;
        if (playerIds?.length === 2 && playerNames?.length === 2) {
          doublesPlayers.push(this.findPlayerInfo(playerIds[0], playerNames[0], placeInGroup, this.PLAYER_A_SIDE));
          if (playerNames[1] !== '') {
            doublesPlayers.push(this.findPlayerInfo(playerIds[1], playerNames[1], placeInGroup, this.PLAYER_B_SIDE));
          }
        }
      }
      const doublesPlayersPerGroup  = this.tournamentEvent.playersPerGroup * 2
      const doublesPlayersLen = doublesPlayers.length;
      if (doublesPlayersPerGroup > doublesPlayersLen) {
        for (let i = doublesPlayersLen; i < doublesPlayersPerGroup; i++) {
          const placeInGroup = Math.floor(i / 2) + 1;
          const playerName = (i % 2 === 0) ? this.PLAYER_A_EMPTY : this.PLAYER_B_EMPTY;
          const playerSide = (i % 2 === 0) ? this.PLAYER_A_SIDE: this.PLAYER_B_SIDE;
          doublesPlayers.push({profileId: this.TBD_PROFILE_ID,
            playerName: playerName, rating: -1, placeInGroup: placeInGroup,
            byeNum: 0, seSeedNumber: 0, singleEliminationLineNum: 0, playerSide: playerSide});
        }
      }
      groupPlayers = doublesPlayers;
    } else {
      if (this.tournamentEvent.playersPerGroup > groupPlayers.length) {
        const groupLen = groupPlayers.length;
        for (let i = groupLen; i < this.tournamentEvent.playersPerGroup; i++) {
          groupPlayers.push({profileId: this.TBD_PROFILE_ID, playerName: '(empty)', rating: -1, placeInGroup: i + 1, byeNum: 0, seSeedNumber: 0,
            singleEliminationLineNum: 0,
            playerSide: null});
        }
      }
    }

    this.groupPlayers = groupPlayers;
  }

  private getFirstRoundPlayerInfos() {
    // console.log('drawItems', this.drawItems);

    let firstRoundPlayers = this.drawItems.map((drawItem: DrawItem): PlayerInfo => {
      return {
        profileId: drawItem.playerId,
        playerName: (drawItem.byeNum != 0) ? `Bye #${drawItem.byeNum}` : drawItem.playerName,
        rating: drawItem.rating,
        placeInGroup: drawItem.placeInGroup,
        byeNum: drawItem.byeNum,
        seSeedNumber: drawItem.seSeedNumber,
        singleEliminationLineNum: drawItem.singleElimLineNum,
        playerSide: null
      };
    });

    // console.log('firstRoundPlayers', firstRoundPlayers);

    if (this.tournamentEvent.doubles) {
      let doublesPlayers: any [] = [];
      for (const doublesTeamPlayers of firstRoundPlayers) {
        const playerIds: string[] = doublesTeamPlayers.profileId.split(';');
        const playerNames: string[] = doublesTeamPlayers.playerName.split(' / ');
        const placeInGroup = doublesTeamPlayers.placeInGroup;
        if (playerIds?.length === 2 && playerNames?.length === 2) {
          doublesPlayers.push(this.findPlayerInfo(playerIds[0], playerNames[0], placeInGroup, this.PLAYER_A_SIDE));
          doublesPlayers.push(this.findPlayerInfo(playerIds[1], playerNames[1], placeInGroup, this.PLAYER_B_SIDE));
        }
      }
      firstRoundPlayers = doublesPlayers;
    }

    this.firstRoundPlayers = firstRoundPlayers;
  }

/**
 * Finds player info for doubles player so we can show individual player ratings
 * @param profileId
 * @param playerName
 * @param placeInGroup
 * @param playerSide
 * @private
 */
  private findPlayerInfo(profileId: string, playerName: string, placeInGroup: number, playerSide: string) {
    let playerInfo: PlayerInfo = {profileId: profileId, playerName: playerName, rating: 0, placeInGroup: placeInGroup, byeNum: 0,
      seSeedNumber: 0, singleEliminationLineNum: 0, playerSide: playerSide};
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
   * @param eventId
   * @private
   */
  private fetchReplacementPlayers(eventId: number) {
    this.isLoading = true;
    this.tournamentEntryInfoService.getReplacementPlayersForEvent(eventId)
      .pipe(first())
      .subscribe({
        next: (entryInfos: TournamentEntryInfo[]) => {
          this.entryInfos = entryInfos;
          const filteredPlayers = entryInfos.map((entryInfo: TournamentEntryInfo) => {
            const playerInfo: PlayerInfo = {
              playerName: `${entryInfo.lastName}, ${entryInfo.firstName}`,
              playerSide: null,
              profileId: entryInfo.profileId,
              rating: entryInfo.seedRating,
              placeInGroup: 0, byeNum: 0, seSeedNumber: 0, singleEliminationLineNum: 0
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
   * Pays payment
   * @param payByCreditCard
   */
  onPay(payByCreditCard: boolean) {
    const profileId = this.getPlayerToAddProfileId();
    this.profileService.getProfile(profileId)
      .pipe(first())
      .subscribe({
        next: (profile: Profile) => {
          this.doPayment(profile, payByCreditCard);
        },
        error: (error: any) => {
          const message = 'Error getting profile ' + profileId + ' ' + error;
          this.errorMessagePopupService.showError(message);
        },
        complete: () => {}
      });
  }

  private getPlayerToAddProfileId (): string {
    return this.playerToAdd[0];
  }

  /**
   *
   * @param scope
   */
  onPaymentSuccessful(scope: any) {
    const thisPopup: ReplacePlayerPopupComponent = scope;
    thisPopup.paymentCompleted = true;
  }

  /**
   *
   * @param scope
   */
  onPaymentCanceled(scope: any) {
  }

  /**
   *
   * @param profile
   * @param payByCreditCard
   * @private
   */
  private doPayment(profile: Profile, payByCreditCard: boolean) {
    const profileId = this.getPlayerToAddProfileId();
    const entryInfo = this.findEntryInfo(profileId);
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
      currencyCode: this.tournamentCurrency,
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

    if (payByCreditCard == true) {
      this.paymentDialogService.showPaymentDialog(paymentDialogData, callbackData);
    } else {
      this.checkCashPaymentDialogService.showPaymentDialog(paymentDialogData, callbackData);
    }
  }

  private findEntryInfo (profileId: string): TournamentEntryInfo {
    let entryToReturn  = null;
    if (this.entryInfos != null) {
      const foundEI = this.entryInfos.filter((entryInfo: TournamentEntryInfo) => {
        return entryInfo.profileId === profileId;
      });
      if (foundEI?.length === 1) {
        entryToReturn = foundEI[0];
      }
    }
    return entryToReturn;
  }

  /**
   *
   * @param playerInfo
   * @private
   */
  private findDrawItem(playerInfo: PlayerInfo): DrawItem {
    const playerDrawItems = this.drawItems.filter(
      (drawItem: DrawItem) => {
        if (this.tournamentEvent.doubles) {
          if (this.drawType === DrawType.ROUND_ROBIN) {
            return (drawItem.playerId.indexOf(playerInfo.profileId) !== -1) && drawItem.groupNum == this.selectedGroup;
          } else {
            return (drawItem.playerId.indexOf(playerInfo.profileId) !== -1) && drawItem.singleElimLineNum === playerInfo.singleEliminationLineNum;
          }
        } else {
          if (this.drawType === DrawType.ROUND_ROBIN) {
            return drawItem.playerId === playerInfo.profileId && drawItem.groupNum == this.selectedGroup;
          } else {
            return drawItem.playerId === playerInfo.profileId && drawItem.singleElimLineNum === playerInfo.singleEliminationLineNum;
          }
        }
      });
    // console.log('playerDrawItems', playerDrawItems);
    return (playerDrawItems?.length === 1) ? playerDrawItems[0] : this.makeEmptyDrawItem(
      playerInfo.profileId, playerInfo.placeInGroup, playerInfo.playerName);
  }

  /**
   * Makes an empty draw item
   * @param profileId
   * @param placeInGroup
   * @param playerName
   * @private
   */
  private makeEmptyDrawItem(profileId: string, placeInGroup: number, playerName: string): DrawItem {
    return {
      id: null,
      eventFk: this.tournamentEvent.id,
      groupNum: this.selectedGroup,
      round: 0,
      placeInGroup: placeInGroup,
      seSeedNumber: 0,
      byeNum: 0,
      singleElimLineNum: 0,
      drawType: this.drawType,
      playerId: profileId,
      conflictType: ConflictType.NO_CONFLICT,
      rating: 0,
      // transient values
      playerName: playerName,
      clubName: null,
      state: null,
      entryId: 0
    };
  }

}

export interface ReplacePlayerPopupData {
  drawItems: DrawItem[];
  tournamentEvent: TournamentEvent;
  tournamentName: string;
  drawType: DrawType;
}

export interface ReplacePlayerRequest {
  // draw item to replace
  drawItem: DrawItem;
  // entry id of a player to be placed in this draw item
  playerToAddEntryId: number;
  // player side to replace in doubles A or B, null in singles
  playerSideToRemove: string;
}

export interface PlayerInfo {
  placeInGroup: number;
  playerSide: string;  // for doubles A or B, for singles null
  playerName: string;
  profileId: string;
  rating: number;
  byeNum: number;
  seSeedNumber: number;
  singleEliminationLineNum: number;
}
