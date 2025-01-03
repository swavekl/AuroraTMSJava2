import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {DrawItem} from '../../draws-common/model/draw-item.model';
import {TournamentEvent} from '../../../tournament/tournament-config/tournament-event.model';
import {TournamentEntryInfoService} from '../../../tournament/service/tournament-entry-info.service';
import {first} from 'rxjs/operators';
import {TournamentEntryInfo} from '../../../tournament/model/tournament-entry-info.model';
import {DrawType} from '../../draws-common/model/draw-type.enum';

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
  groupPlayers: PlayerInfo [] = [];
  filterPlayerName: string = '';
  filteredPlayers: PlayerInfo [] = [];
  tournamentEvent: TournamentEvent;
  drawItems: DrawItem [];
  isLoading: boolean;

  constructor(public dialogRef: MatDialogRef<ReplacePlayerPopupComponent>,
              @Inject(MAT_DIALOG_DATA) public data: ReplacePlayerPopupData,
              private tournamentEntryInfoService: TournamentEntryInfoService) {
    this.tournamentEvent = data.tournamentEvent;
    this.drawItems = data.drawItems;
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
    const request: ReplacePlayerRequest = {
      playerToAdd: this.playerToAdd[0],
      playerToRemove: this.playerToRemove[0],
      tournamentEventId: this.tournamentEvent.id
    };
    this.dialogRef.close({action: 'ok', request: request});
  }

  onSelectPlayerToRemove(profileId: string) {
    console.log('player to remove', profileId);
  }

  onSelectPlayerToAdd(profileId: any) {
    console.log('player to add', profileId);
  }

  clearFilter() {

  }

  onGroupChange($event: any) {
    const groupNum = $event.value;
    let groupPlayers: PlayerInfo[] = this.drawItems.filter((drawItem: DrawItem): boolean => {
      return (drawItem.groupNum === groupNum) && drawItem.drawType === DrawType.ROUND_ROBIN
        && drawItem.playerName != null && drawItem.playerId != null;
    }).map((drawItem: DrawItem): PlayerInfo => {
      return {profileId: drawItem.playerId, playerName: drawItem.playerName, rating: drawItem.rating};
    });
    if (this.tournamentEvent.doubles) {
      let doublesPlayers: any [] = [];
      for (const doublesTeamPlayers of groupPlayers) {
        const playerIds: string[] = doublesTeamPlayers.profileId.split(';');
        const playerNames: string[] = doublesTeamPlayers.playerName.split(' / ');
        if (playerIds?.length === 2 && playerNames?.length === 2) {
          doublesPlayers.push(this.findPlayerInfo(playerIds[0], playerNames[0]));
          doublesPlayers.push(this.findPlayerInfo(playerIds[1], playerNames[1]));
        }
      }
      groupPlayers = doublesPlayers;
    }

    this.groupPlayers = groupPlayers.sort((pi1: PlayerInfo, pi2: PlayerInfo) => {
      return pi1.playerName.localeCompare(pi2.playerName);
    });
  }

  private findPlayerInfo(profileId: string, playerName: string) {
    let playerInfo: PlayerInfo = {profileId: profileId, playerName: playerName, rating: 0};
    console.log('Finding player rating in doubles', playerName);
    if (this.filteredPlayers != null) {
      const foundPlayers: PlayerInfo[] = this.filteredPlayers.filter((pi: PlayerInfo): boolean => {
        return pi.profileId === profileId;
      });
      if (foundPlayers.length > 0) {
        playerInfo = foundPlayers[0];
      }
    }
    return playerInfo;
  }

  private fetchAllPlayers(tournamentId: number) {
    this.isLoading = true;
    this.tournamentEntryInfoService.getAll(tournamentId)
      .pipe(first())
      .subscribe({
        next: (entryInfos: TournamentEntryInfo[]) => {
          const filteredPlayers = entryInfos.map((entryInfo: TournamentEntryInfo) => {
            const playerInfo: PlayerInfo = {
              playerName: `${entryInfo.lastName}, ${entryInfo.firstName}`,
              profileId: entryInfo.profileId,
              rating: entryInfo.seedRating
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
}

export interface ReplacePlayerPopupData {
  drawItems: DrawItem[];
  tournamentEvent: TournamentEvent;
}

export interface ReplacePlayerRequest {
  playerToRemove: string;
  playerToAdd: string;
  tournamentEventId: number;
}

export interface PlayerInfo {
  playerName: string;
  profileId: string;
  rating: number;
}
