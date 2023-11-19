import {Component, Input, OnChanges, OnDestroy, SimpleChange, SimpleChanges} from '@angular/core';
import {GroupTieBreakingInfo} from '../model/tie-breaking/group-tie-breaking-info.model';
import {TieBreakingResultsDialogComponent} from '../tie-breaking-results-dialog/tie-breaking-results-dialog.component';
import {Subscription} from 'rxjs';
import {MatDialog} from '@angular/material/dialog';
import {TieBreakingService} from '../service/tie-breaking.service';
import {MatchCard} from '../model/match-card.model';
import {Match} from '../model/match.model';

@Component({
  selector: 'app-ranking-results',
  templateUrl: './ranking-results.component.html',
  styleUrls: ['./ranking-results.component.scss']
})
export class RankingResultsComponent implements OnDestroy, OnChanges {
  @Input()
  matchCard: MatchCard;

  rankedPlayerInfos: any [] = [];

  private subscriptions: Subscription = new Subscription();

  constructor(private dialog: MatDialog,
              private tieBreakingService: TieBreakingService) {
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  ngOnChanges(changes: SimpleChanges): void {
    const matchCardChange: SimpleChange = changes.matchCard;
    if (matchCardChange) {
      const matchCard: MatchCard = matchCardChange.currentValue;
      if (matchCard) {
        this.rankedPlayerInfos = this.makeRankedPlayerInfos(matchCard);
      }
    }
  }

  public rankAndAdvance(matchCardId: number) {
    const subscription = this.tieBreakingService.rankAndAdvance(matchCardId)
      .subscribe((groupTieBreakingInfo: GroupTieBreakingInfo) => {
        this.rankedPlayerInfos = this.processTieBreakingInfo(groupTieBreakingInfo);
      });
    this.subscriptions.add(subscription);
  }

  explainRanking() {
      const subscription = this.tieBreakingService.rankAndAdvance(this.matchCard.id)
        .subscribe((groupTieBreakingInfo: GroupTieBreakingInfo) => {
          this.showTieBreakingDialog(groupTieBreakingInfo);
        });
      this.subscriptions.add(subscription);
  }

  private showTieBreakingDialog(groupTieBreakingInfo: GroupTieBreakingInfo) {
    const numPlayers = groupTieBreakingInfo.playerTieBreakingInfoList.length;
    let width = (numPlayers + 2) * 75;
    width = Math.max(width, 520);
    width = Math.min(width, window.innerWidth - 100);
    let height = (numPlayers * 50) + 180;
    if (groupTieBreakingInfo.nwayTieBreakingInfosMap != null) {
      for (const [title, playerTieBreakingInfoList] of Object.entries(groupTieBreakingInfo.nwayTieBreakingInfosMap)) {
        height += (playerTieBreakingInfoList.length * 50) + 75;
      }
    }
    height = Math.max(height, 400);
    height = Math.min(height, window.innerHeight - 100);
    const strHeight = height + 'px';
    const strWidth = width + 'px';
    // console.log('strWidth', strWidth);
    // console.log('strHeight', strHeight);
    const config = {
      width: strWidth, height: strHeight, data: groupTieBreakingInfo
    };

    this.dialog.open(TieBreakingResultsDialogComponent, config);
  }

  public makeRankedPlayerInfos(selectedMatchCard: MatchCard): any[] {
    const rankedPlayerInfos = [];
    // make map of player profile id to letter code A, B, C ...
    const matches = selectedMatchCard?.matches;
    const profileIdToPlayerLetterMap = {};
    matches.forEach((match: Match) => {
      profileIdToPlayerLetterMap[match.playerAProfileId] = match.playerALetter;
      profileIdToPlayerLetterMap[match.playerBProfileId] = match.playerBLetter;
    });

    // build ranking table information
    const playerRankingsJSON = selectedMatchCard?.playerRankings;
    if (playerRankingsJSON && playerRankingsJSON?.length > 0) {
      const playerRankings = JSON.parse(playerRankingsJSON);
      if (Object.keys(playerRankings).length > 0) {
        const profileIdToNameMap = selectedMatchCard?.profileIdToNameMap;
        for (const [rank, playerProfileId] of Object.entries(playerRankings)) {
          const playerCode = profileIdToPlayerLetterMap[playerProfileId as string];
          const playerName = profileIdToNameMap[playerProfileId as string];
          rankedPlayerInfos.push({
            rank: rank,
            playerCode: playerCode,
            playerName: playerName
          });
        }
      }
    }

    return this.sortRankedPlayerInfos(rankedPlayerInfos);
  }

  private processTieBreakingInfo(groupTieBreakingInfo: GroupTieBreakingInfo): any [] {
    const playerTieBreakingInfoList = groupTieBreakingInfo.playerTieBreakingInfoList ?? [];
    const rankedPlayerInfos = [];
    for (let i = 0; i < playerTieBreakingInfoList.length; i++) {
      const playerTieBreakingInfo = playerTieBreakingInfoList[i];
      const playerName = groupTieBreakingInfo.profileIdToNameMap[playerTieBreakingInfo.playerProfileId];
      rankedPlayerInfos.push({
        rank: playerTieBreakingInfo.rank,
        playerCode: playerTieBreakingInfo.playerCode,
        playerName: playerName
      });
    }
    return this.sortRankedPlayerInfos(rankedPlayerInfos);
  }

  private sortRankedPlayerInfos(rankedPlayerInfos): any [] {
    rankedPlayerInfos.sort((player1: any, player2: any) => {
      return (player1.rank > player2.rank) ? 1 : ((player1.rank < player2.rank) ? -1 : 0);
    });
    return rankedPlayerInfos;
  }
}
