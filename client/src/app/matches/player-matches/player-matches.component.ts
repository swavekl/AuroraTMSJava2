import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  QueryList,
  SimpleChange,
  SimpleChanges,
  ViewChildren
} from '@angular/core';
import {MatchCard} from '../model/match-card.model';
import {Match} from '../model/match.model';
import {AuthenticationService} from '../../user/authentication.service';
import {ConfirmationPopupComponent} from '../../shared/confirmation-popup/confirmation-popup.component';
import {MatDialog} from '@angular/material/dialog';
import {MatCheckbox} from '@angular/material/checkbox';
import {MatchCardStatus} from '../model/match-card-status.enum';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {DefaultPlayerPhoneDialogComponent} from '../default-player-phone-dialog/default-player-phone-dialog.component';

@Component({
  selector: 'app-player-matches',
  templateUrl: './player-matches.component.html',
  styleUrls: ['./player-matches.component.scss']
})
export class PlayerMatchesComponent implements OnInit, OnChanges {

  @Input()
  public matchCard: MatchCard;

  @Input()
  public tournamentEvent: TournamentEvent;

  @Input()
  public tournamentId: number;

  @Input()
  public pointsPerGame: number;

  private thisPlayerProfileId: string;

  @Input()
  public doubles: boolean;

  @Input()
  private expandedMatchIndex: number;

  @Output()
  public back: EventEmitter<any> = new EventEmitter<any>();

  @Output()
  public enterMatchScore: EventEmitter<any> = new EventEmitter<any>();

  @Output()
  public updateMatch: EventEmitter<Match> = new EventEmitter<Match>();

  @Output()
  public rankings: EventEmitter<any> = new EventEmitter<any>();

  @Output()
  public refresh: EventEmitter<any> = new EventEmitter<any>();

  // array so we can use iteration in the template
  games: number [];

  @ViewChildren(MatCheckbox)
  defaultCheckboxElementRefs: QueryList<MatCheckbox>;

  rankingsAvailable: boolean = false;

  constructor(private authenticationService: AuthenticationService,
              private dialog: MatDialog) {
    this.expandedMatchIndex = 0;
    this.games = [];
    this.pointsPerGame = 11;
    this.thisPlayerProfileId = this.authenticationService.getCurrentUserProfileId();
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    const matchCardChanges: SimpleChange = changes.matchCard;
    if (matchCardChanges) {
      const matchCard = matchCardChanges.currentValue;
      if (matchCard) {
        const numGames = matchCard.numberOfGames === 0 ? 5 : matchCard.numberOfGames;
        this.games = Array(numGames);
      }
    }

    const tournamentEventChanges: SimpleChange = changes.tournamentEvent;
    if (tournamentEventChanges) {
      this.tournamentEvent = tournamentEventChanges.currentValue;
    }

    if (this.tournamentEvent != null && this.matchCard != null) {
      this.rankingsAvailable = MatchCard.isMatchCardCompleted(this.matchCard, this.tournamentEvent);
    }
  }

  isMatchExpanded(index: number): boolean {
    return this.expandedMatchIndex === index;
  }

  expandMatch(index: number) {
    this.expandedMatchIndex = index;
  }

  isMatchWinner(match: Match, profileId: string): boolean {
    return (match) ? Match.isMatchWinner(profileId, match, this.matchCard.numberOfGames, this.pointsPerGame) : false;
  }

  isPlayerMatch(match: Match): boolean {
    return (match.playerAProfileId.indexOf(this.thisPlayerProfileId) >= 0) ||
           (match.playerBProfileId.indexOf(this.thisPlayerProfileId) >= 0);
  }

  isNameBolded(profileIds: string): boolean {
    // console.log('profileIds: ' + profileIds + ' this.thisPlayerProfileId: ' + this.thisPlayerProfileId);
    return profileIds.indexOf(this.thisPlayerProfileId) >= 0;
  }

  isDoublesPlayerNameBolded(profileIds: string, index: number): boolean {
    // console.log('profileIds: ' + profileIds + ' this.thisPlayerProfileId: ' + this.thisPlayerProfileId);
    const profileIdsArray = profileIds.split(';');
    const doublesPlayerId = profileIdsArray[index];
    return doublesPlayerId === this.thisPlayerProfileId;
  }

  getDoublesPlayerName (profileIds: string, index: number): string {
    const playerNames: string = this.matchCard.profileIdToNameMap[profileIds];
    const playerNamesArray = playerNames.split('/');
    return playerNamesArray[index].trim();
  }

  getPlayerName(profileId: string): string {
    return this.matchCard.profileIdToNameMap[profileId];
  }

  onBack() {
    this.back.emit(null);
  }
  onRefresh() {
    this.refresh.emit(null);
  }

  onViewRankings() {
    this.rankings.emit(null);
  }

  getCheckboxId(matchIndex: number, playerIndex: number): string {
    return `defaultCB_${matchIndex}_${playerIndex}`;
  }

  /**
   * Sets default status for player A or B
   * @param match match to set this in
   * @param matchIndex match index in array of matches
   * @param playerIndex index of player to default
   */
  defaultPlayer(match: Match, matchIndex: number, playerIndex: number) {
    if (!this.canChangeScore(match)) {
      this.revertToggle(matchIndex, playerIndex);
    } else {
      if (this.isMatchEntryStarted(match)) {
        const profileIds = (playerIndex === 0) ? match.playerAProfileId : match.playerBProfileId;
        const playerNames: string = this.matchCard.profileIdToNameMap[profileIds];
        const message = `${playerNames} is about to be defaulted. Defaulting will set all remaining game scores to 0 and it can't be undone.  Press \'OK\' to proceed or 'Cancel' to abort.`;
        const config = {
          width: '450px', height: '260px', data: {
            message: message, contentAreaHeight: 140
          }
        };
        const dialogRef = this.dialog.open(ConfirmationPopupComponent, config);
        dialogRef.afterClosed().subscribe(result => {
          if (result === 'ok') {
            this.proceedWithDefaulting(match, playerIndex);
          } else {
            this.revertToggle(matchIndex, playerIndex);
          }
        });
      } else {
        this.proceedWithDefaulting(match, playerIndex);
      }
    }
  }

  private revertToggle(matchIndex: number, playerIndex: number) {
    // revert toggle
    const checkboxId = this.getCheckboxId(matchIndex, playerIndex);
    this.defaultCheckboxElementRefs.forEach((matCheckbox: MatCheckbox) => {
      if (matCheckbox.id === checkboxId) {
        matCheckbox.toggle();
      }
    });
  }

  /**
   *
   * @param match
   * @private
   */
  private isMatchEntryStarted(match: Match): boolean {
    return (match.game1ScoreSideA !== 0 || match.game1ScoreSideB !== 0) ||
      (match.game2ScoreSideA !== 0 || match.game2ScoreSideB !== 0) ||
      (match.game3ScoreSideA !== 0 || match.game3ScoreSideB !== 0) ||
      (match.game4ScoreSideA !== 0 || match.game4ScoreSideB !== 0) ||
      (match.game5ScoreSideA !== 0 || match.game5ScoreSideB !== 0) ||
      (match.game6ScoreSideA !== 0 || match.game6ScoreSideB !== 0) ||
      (match.game7ScoreSideA !== 0 || match.game7ScoreSideB !== 0);
  }

  /**
   *
   * @param match
   * @param playerIndex
   * @private
   */
  private proceedWithDefaulting (match: Match, playerIndex: number) {
    const numberOfGames: number = this.games.length;
    const updatedMatch = Match.defaultMatch(match, playerIndex, numberOfGames, this.pointsPerGame);
    // console.log('proceedWithDefaulting updatedMatch', updatedMatch);
    this.updateMatch.emit(updatedMatch);
  }

  canChangeScore(match: Match): boolean {
    const canChange = (match?.scoreEnteredByProfileId == null || match?.scoreEnteredByProfileId == '' || match?.scoreEnteredByProfileId === this.thisPlayerProfileId);
    if (!canChange) {
      const message = `You can't enter/change score because, another player or tournament official already started entering score.`;
      const config = {
        width: '300px', height: '240px', data: { title: 'Error',
          message: message, contentAreaHeight: 80, showCancel: false,
          okText: 'Close'
        }
      };
      const dialogRef = this.dialog.open(ConfirmationPopupComponent, config);
      dialogRef.afterClosed().subscribe(result => {
      });
    }
    return canChange;
  }

  public onEnterScore(matchIndex: number) {
    const match = this.matchCard?.matches[matchIndex];
    if (this.canChangeScore(match)) {
      this.enterMatchScore.emit({matchIndex: matchIndex, matchId: match.id});
    }
  }

  isEventStarted(): boolean {
    return this.matchCard.status === MatchCardStatus.STARTED;
  }

  onDefaultMatch(matchIndex: number) {
    const match = this.matchCard?.matches[matchIndex];
    if (this.canChangeScore(match)) {
      const playerAName: string = this.matchCard.profileIdToNameMap[match.playerAProfileId];
      const playerBName: string = this.matchCard.profileIdToNameMap[match.playerBProfileId];
      const data = {
        doubles: this.doubles,
        playerALetter: match.playerALetter,
        playerBLetter: match.playerBLetter,
        playerAName: playerAName,
        playerBName: playerBName
      };

      const config = {
        width: '400px', height: '300px', data: data
      };
      const dialogRef = this.dialog.open(DefaultPlayerPhoneDialogComponent, config);
      dialogRef.afterClosed().subscribe(result => {
        if (result.action === 'save') {
          const playerIndex = result.defaultedPlayerIndex;
          if (this.isMatchEntryStarted(match)) {
            const profileIds = (playerIndex === 0) ? match.playerAProfileId : match.playerBProfileId;
            const playerNames: string = this.matchCard.profileIdToNameMap[profileIds];
            const message = `${playerNames} is about to be defaulted. Defaulting will set all game scores to 0 and it can't be undone.  Press \'OK\' to proceed or 'Cancel' to abort.`;
            const config = {
              width: '450px', height: '260px', data: {
                message: message, contentAreaHeight: 140
              }
            };
            const dialogRef = this.dialog.open(ConfirmationPopupComponent, config);
            dialogRef.afterClosed().subscribe(result => {
              if (result === 'ok') {
                this.proceedWithDefaulting(match, playerIndex);
              }
            });
          } else {
            this.proceedWithDefaulting(match, playerIndex);
          }
        }
      });
    }
  }
}
