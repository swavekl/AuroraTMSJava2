import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChange, SimpleChanges} from '@angular/core';
import {MatDialogRef} from '@angular/material/dialog/dialog-ref';
import {MatDialog} from '@angular/material/dialog';
import {Subscription} from 'rxjs';
import {first, switchMap} from 'rxjs/operators';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {MatchCard} from '../model/match-card.model';
import {Match} from '../model/match.model';
import {ScoreEntryDialogComponent} from '../score-entry-dialog/score-entry-dialog.component';
import {ScoreEntryDialogData, ScoreEntryDialogResult} from '../score-entry-dialog/score-entry-dialog-data';
import {MatchService} from '../service/match.service';
import {TieBreakingService} from '../service/tie-breaking.service';
import {GroupTieBreakingInfo} from '../model/tie-breaking/group-tie-breaking-info.model';

@Component({
  selector: 'app-matches',
  templateUrl: './matches.component.html',
  styleUrls: ['./matches.component.scss']
})
export class MatchesComponent implements OnInit, OnChanges, OnDestroy {

  @Input()
  tournamentName: string;

  @Input()
  tournamentEvents: TournamentEvent[] = [];

  @Input()
  matchCards: MatchCard[] = [];

  @Input()
  selectedMatchCard: MatchCard;

  @Input()
  rankedPlayerInfos: any [] = [];

  @Output()
  private tournamentEventEmitter: EventEmitter<any> = new EventEmitter<any>();

  @Output()
  private matchCardEmitter: EventEmitter<any> = new EventEmitter<any>();

  // currently selected event for viewing draws
  selectedEvent: TournamentEvent;

  // id of the selected match card
  selectedMatchCardId: number;

  // array so we can use iteration in the template
  games: number [];

  private subscriptions: Subscription;

  private scoreEntryDialogRef: MatDialogRef<ScoreEntryDialogComponent>;

  constructor(private dialog: MatDialog,
              private matchService: MatchService,
              private tieBreakingService: TieBreakingService) {
    this.games = [];
    this.subscriptions = new Subscription();
  }

  ngOnInit(): void {
  }

  ngOnDestroy() {
    this.subscriptions.unsubscribe();
  }

  onSelectEvent(tournamentEvent: TournamentEvent) {
    // load match cards for this event
    this.selectedMatchCardId = -1;
    this.selectedMatchCard = null;
    this.selectedEvent = tournamentEvent;
    this.tournamentEventEmitter.emit(tournamentEvent.id);
  }

  isSelected(tournamentEvent: TournamentEvent) {
    return tournamentEvent.id === this.selectedEvent?.id;
  }

  ngOnChanges(changes: SimpleChanges): void {
    const tournamentEventsChanges: SimpleChange = changes.tournamentEvents;
    if (tournamentEventsChanges) {
      const te = tournamentEventsChanges.currentValue;
      // console.log('DrawsComponent got tournament events of length ' + te.length);
    }
    const selectedMatchCardChange: SimpleChange = changes.selectedMatchCard;
    if (selectedMatchCardChange) {
      const selectedMatchCard: MatchCard = selectedMatchCardChange.currentValue;
      if (selectedMatchCard) {
        this.rankedPlayerInfos = this.makeRankedPlayerInfos(selectedMatchCard);
      }
    }
  }

  onSelectMatchCard(matchCard: MatchCard) {
    this.selectedMatchCardId = matchCard.id;
    this.matchCardEmitter.emit(this.selectedMatchCardId);
    const numGames = matchCard.numberOfGames === 0 ? 5 : matchCard.numberOfGames;
    this.games = Array(numGames);
  }

  isSelectedMatchCard(matchCard: MatchCard): boolean {
    return (this.selectedMatchCardId === matchCard.id);
  }

  /**
   * Launches dialog to enter/edit match score
   * @param match
   * @param matchIndex
   */
  onMatchScoreEntry(match: Match, matchIndex: number) {
    const data: ScoreEntryDialogData = this.makeMatchDialogData(match, matchIndex);
    const config = {
      width: '835px', height: '450px', data: data
    };

    this.scoreEntryDialogRef = this.dialog.open(ScoreEntryDialogComponent, config);
    const subscription = this.scoreEntryDialogRef.afterClosed().subscribe((result: ScoreEntryDialogResult) => {
      this.matchService.update(result.match)
        .pipe(first())
        .subscribe(
          (updatedMatch: Match) => {
            this.matchCardEmitter.emit(this.selectedMatchCardId);
          }
        );
    });
    this.subscriptions.add(subscription);
  }

  /**
   * Entry point for calling back from dialog
   * @param scope
   * @param result
   */
  public onPreviousNextCallback(scope: any, result: ScoreEntryDialogResult) {
    scope.movePreviousNext(result.action, result.match);
  }

  /**
   * member function for calling back from score entry dialog
   * @param action
   * @param currentlyEditedMatch
   * @private
   */
  private movePreviousNext(action: string, currentlyEditedMatch: Match) {
    let nextMatchId = 0;
    let nextMatchIndex = 0;
    for (let i = 0; i < this.selectedMatchCard.matches.length; i++) {
      const match = this.selectedMatchCard.matches[i];
      if (match.id === currentlyEditedMatch.id) {
        nextMatchIndex = (action === 'next') ? i + 1 : i - 1;
        nextMatchId = this.selectedMatchCard.matches[nextMatchIndex].id;
      }
    }
    // save match that was edited
    const subscription: Subscription = this.matchService.update(currentlyEditedMatch)
      .pipe(switchMap(() => {
        // load the next match
        return this.matchService.getByKey(nextMatchId);
      }))
      .subscribe((nextMatchToEdit: Match) => {
        const scoreEntryDialogData: ScoreEntryDialogData = this.makeMatchDialogData(nextMatchToEdit, nextMatchIndex);
        const dialogComponentInstance = this.scoreEntryDialogRef.componentInstance;
        dialogComponentInstance.displayMatch(scoreEntryDialogData);
      });
    this.subscriptions.add(subscription);
  }

  /**
   *
   * @param match
   * @param nextMatchIndex
   * @private
   */
  private makeMatchDialogData(match: Match, nextMatchIndex: number): ScoreEntryDialogData {
    return {
      match: match,
      numberOfGames: this.selectedMatchCard.numberOfGames,
      playerAName: this.selectedMatchCard.profileIdToNameMap[match.playerAProfileId],
      playerBName: this.selectedMatchCard.profileIdToNameMap[match.playerBProfileId],
      numberOfMatchesInCard: this.selectedMatchCard?.matches?.length || 0,
      editedMatchIndex: nextMatchIndex,
      callbackFn: this.onPreviousNextCallback,
      callbackFnScope: this,
      pointsPerGame: this.selectedEvent.pointsPerGame
    };
  }

  public isMatchWinner(profileId: string, match: Match): boolean {
    return Match.isMatchWinner(profileId, match, this.selectedMatchCard?.numberOfGames, this.selectedEvent?.pointsPerGame);
  }

  public getRoundShortName(round: number): string {
    return MatchCard.getRoundShortName(round);
  }

  public rankAndAdvance() {
    const subscription = this.tieBreakingService.rankAndAdvance(this.selectedMatchCardId)
      .subscribe((groupTieBreakingInfo: GroupTieBreakingInfo) => {
        this.rankedPlayerInfos = this.processTieBreakingInfo(groupTieBreakingInfo);
      });
    this.subscriptions.add(subscription);
  }

  /**
   * Processes incoming rankings information
   * @param groupTieBreakingInfo
   * @private
   */
  private processTieBreakingInfo(groupTieBreakingInfo: GroupTieBreakingInfo) {
    const playerTieBreakingInfoList = groupTieBreakingInfo.playerTieBreakingInfoList ?? [];
    const rankedPlayerInfos = [];
    for (let i = 0; i < playerTieBreakingInfoList.length; i++) {
      const playerTieBreakingInfo = playerTieBreakingInfoList[i];
      const playerName = this.selectedMatchCard.profileIdToNameMap[playerTieBreakingInfo.playerProfileId];
      rankedPlayerInfos.push({
        rank: playerTieBreakingInfo.rank,
        playerCode: playerTieBreakingInfo.playerCode,
        playerName: playerName
      });
    }
    return this.sortRankedPlayerInfos(rankedPlayerInfos);
  }

  /**
   * Makes ranking information from data saved in match card
   * @param selectedMatchCard
   * @private
   */
  private makeRankedPlayerInfos(selectedMatchCard: MatchCard): any[] {
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

  private sortRankedPlayerInfos(rankedPlayerInfos) {
    rankedPlayerInfos.sort((player1: any, player2: any) => {
      return (player1.rank > player2.rank) ? 1 : ((player1.rank < player2.rank) ? -1 : 0);
    });
    return rankedPlayerInfos;
  }
}
