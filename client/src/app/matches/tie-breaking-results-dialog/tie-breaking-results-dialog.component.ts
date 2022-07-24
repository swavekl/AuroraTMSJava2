import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {GroupTieBreakingInfo} from '../model/tie-breaking/group-tie-breaking-info.model';
import {MatchStatus} from '../model/tie-breaking/match-status';
import {PlayerTieBreakingInfo} from '../model/tie-breaking/player-tie-breaking-info.model';

@Component({
  selector: 'app-tie-breaking-results-dialog',
  templateUrl: './tie-breaking-results-dialog.component.html',
  styleUrls: ['./tie-breaking-results-dialog.component.scss']
})
export class TieBreakingResultsDialogComponent implements OnInit {

  public dataAsArray: any [];

  constructor(public dialogRef: MatDialogRef<TieBreakingResultsDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: GroupTieBreakingInfo) {

    this.dataAsArray = [];
    this.dataAsArray.push({title: '', playerTieBreakingInfoList: data.playerTieBreakingInfoList,
      showGamesRatio: false, showPointsRatio: false});
    if (data.nwayTieBreakingInfosMap != null) {
      for (const [title, playerTieBreakingInfoList] of Object.entries(data.nwayTieBreakingInfosMap)) {
        const showGamesRatio = this.isShowGamesRatio(playerTieBreakingInfoList);
        const showPointsRatio = this.isShowPointsRatio(playerTieBreakingInfoList);
        this.dataAsArray.push({title: title, playerTieBreakingInfoList: playerTieBreakingInfoList,
          showGamesRatio: showGamesRatio, showPointsRatio: showPointsRatio});
      }
    }
  }

  private isShowGamesRatio(playerTieBreakingInfoList: PlayerTieBreakingInfo[]) {
    let show = false;
    playerTieBreakingInfoList.forEach((info) => {
      if (info.gamesWon !== 0 && info.gamesLost !== 0) {
        show = true;
      }
    });
    return show;
  }

  private isShowPointsRatio(playerTieBreakingInfoList: PlayerTieBreakingInfo[]) {
    let show = false;
    playerTieBreakingInfoList.forEach((info) => {
      if (info.pointsWon !== 0 && info.pointsLost !== 0) {
        show = true;
      }
    });
    return show;
  }

  ngOnInit(): void {
  }

  onClose() {
    this.dialogRef.close(null);
  }

  getMatchStatus(matchStatus: MatchStatus) {
    switch (matchStatus) {
      case MatchStatus.WIN:
        return 'W';
      case MatchStatus.LOSS:
        return 'L';
      case MatchStatus.NOT_PLAYED:
        return 'NP';
    }
  }

  getGameScores(gameScores: number[], matchStatus: MatchStatus) {
    let gamesWon = 0;
    let gamesLost = 0;
    if (gameScores != null) {
      for (let i = 0; i < gameScores.length; i++) {
        const gameScore = gameScores[i];
        if (gameScore < 0) {
          // game lost
          if (matchStatus === MatchStatus.WIN) {
            gamesLost++;
          } else {
            gamesWon++;
          }
        } else {
          // game won
          if (matchStatus === MatchStatus.WIN) {
            gamesWon++;
          } else {
            gamesLost++;
          }
        }
      }
    }

    let strGameScores;
    if (matchStatus === MatchStatus.WIN) {
      strGameScores = gamesWon + ' : ' + gamesLost;
    } else if (matchStatus === MatchStatus.LOSS) {
      strGameScores = gamesLost + ' : ' + gamesWon;
    } else {
      strGameScores = 'Def';
    }
    return strGameScores;
  }

}
