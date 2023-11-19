import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChange, SimpleChanges, ViewChild} from '@angular/core';
import {MatDialog, MatDialogRef} from '@angular/material/dialog';
import {Subscription} from 'rxjs';
import {first, switchMap} from 'rxjs/operators';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {MatchCard} from '../model/match-card.model';
import {Match} from '../model/match.model';
import {ScoreEntryDialogComponent} from '../score-entry-dialog/score-entry-dialog.component';
import {ScoreEntryDialogData, ScoreEntryDialogResult} from '../score-entry-dialog/score-entry-dialog-data';
import {MatchService} from '../service/match.service';
import {DrawType} from '../../draws/draws-common/model/draw-type.enum';
import {RankingResultsComponent} from '../ranking-results/ranking-results.component';

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

  private performRankAndAdvance = false;

  @ViewChild(RankingResultsComponent)
  private rankingResultsComponent: RankingResultsComponent = null;

  constructor(private dialog: MatDialog,
              private matchService: MatchService) {
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

    const matchCardsChange: SimpleChange = changes.matchCards;
    if (matchCardsChange) {
      const matchCards: MatchCard [] = matchCardsChange.currentValue;
      if (matchCards) {
        this.matchCards = matchCards.sort((left: MatchCard, right: MatchCard) => {
          if (left.drawType === DrawType.ROUND_ROBIN && right.drawType === DrawType.SINGLE_ELIMINATION) {
            return -1;
          } else if (left.drawType === DrawType.SINGLE_ELIMINATION && right.drawType === DrawType.ROUND_ROBIN) {
            return 1;
          } else {
            if (left.drawType === DrawType.ROUND_ROBIN) {
              return (left.groupNum === right.groupNum) ? 0 : ((left.groupNum > right.groupNum) ? 1 : -1);
            } else {
              // round of 16
              if (left.round === right.round) {
                const leftMatchNum = left.matches[0].matchNum;
                const rightMatchNum = right.matches[0].matchNum;
                return (leftMatchNum === rightMatchNum) ? 0 : (leftMatchNum > rightMatchNum) ? 1 : -1;
              } else {
                return (left.round < right.round) ? 1 : -1;
              }
            }
          }
        } );
      }
    }

    if (this.rankingResultsComponent != null && this.selectedMatchCard != null) {
      if (this.performRankAndAdvance || this.selectedMatchCard.playerRankings == null) {
        if (this.isSelectedMatchCardCompleted()) {
          this.rankAndAdvance();
        }
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
      width: '860px', height: '500px', data: data
    };

    this.scoreEntryDialogRef = this.dialog.open(ScoreEntryDialogComponent, config);
    const subscription = this.scoreEntryDialogRef.afterClosed().subscribe((result: ScoreEntryDialogResult) => {
      if (result.action === 'ok') {
        this.matchService.update(result.match)
          .pipe(first())
          .subscribe(
            (updatedMatch: Match) => {
              // after updated match card is loaded we need to rank and advance if ready
              this.performRankAndAdvance = true;
              this.matchCardEmitter.emit(this.selectedMatchCardId);
            }
          );
      } else if (result.action === 'refresh') {
        this.performRankAndAdvance = true;
        this.matchCardEmitter.emit(this.selectedMatchCardId);
      }
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
    const matchIdentifier = MatchCard.getFullMatchName(this.selectedEvent.name,
      this.selectedMatchCard.drawType, this.selectedMatchCard.round, this.selectedMatchCard.groupNum);
    return {
      match: match,
      numberOfGames: this.selectedMatchCard.numberOfGames,
      playerAName: this.selectedMatchCard.profileIdToNameMap[match.playerAProfileId],
      playerBName: this.selectedMatchCard.profileIdToNameMap[match.playerBProfileId],
      playerARating: match.playerARating,
      playerBRating: match.playerBRating,
      numberOfMatchesInCard: this.selectedMatchCard?.matches?.length || 0,
      editedMatchIndex: nextMatchIndex,
      callbackFn: this.onPreviousNextCallback,
      callbackFnScope: this,
      matchIdentifier: matchIdentifier,
      drawType: this.selectedMatchCard.drawType,
      pointsPerGame: this.selectedEvent.pointsPerGame
    };
  }

  public isMatchWinner(profileId: string, match: Match): boolean {
    return Match.isMatchWinner(profileId, match, this.selectedMatchCard?.numberOfGames, this.selectedEvent?.pointsPerGame);
  }

  public getMatchIdentifier(round: number, groupNum: number): string {
    return MatchCard.getMatchName(round, groupNum);
  }

  public rankAndAdvance() {
    if (this.rankingResultsComponent != null) {
      this.rankingResultsComponent.rankAndAdvance(this.selectedMatchCardId);
      this.performRankAndAdvance = false;
    }
  }

  /**
   *
   * @private
   */
  private isSelectedMatchCardCompleted(): boolean {
    return MatchCard.isMatchCardCompleted(this.selectedMatchCard, this.selectedEvent);
  }

  public isMatchCardCompleted(matchCard: MatchCard, event: TournamentEvent): boolean {
    return MatchCard.isMatchCardCompleted(matchCard, event);
  }

  isMatchReady(match: Match) {
    return match.playerBProfileId !== Match.TBD_PROFILE_ID && match.playerAProfileId !== Match.TBD_PROFILE_ID;
  }

}
