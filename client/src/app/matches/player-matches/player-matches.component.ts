import {
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output, QueryList,
  SimpleChange,
  SimpleChanges,
  ViewChildren
} from '@angular/core';
import {MatchCard} from '../model/match-card.model';
import {Match} from '../model/match.model';
import {AuthenticationService} from '../../user/authentication.service';
import {ConfirmationPopupComponent} from '../../shared/confirmation-popup/confirmation-popup.component';
import {MatDialog} from '@angular/material/dialog';
import {MatInput} from '@angular/material/input';
import {MatCheckbox} from '@angular/material/checkbox';

@Component({
  selector: 'app-player-matches',
  templateUrl: './player-matches.component.html',
  styleUrls: ['./player-matches.component.scss']
})
export class PlayerMatchesComponent implements OnInit, OnChanges {

  @Input()
  public matchCard: MatchCard;

  @Input()
  public tournamentId: number;

  @Input()
  public pointsPerGame: number;

  @Input()
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

  // array so we can use iteration in the template
  games: number [];

  @ViewChildren(MatCheckbox)
  defaultCheckboxElementRefs: QueryList<MatCheckbox>;

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
    if (this.isMatchEntryStarted(match)) {
      const profileIds = (playerIndex === 0) ? match.playerAProfileId : match.playerBProfileId;
      const playerNames: string = this.matchCard.profileIdToNameMap[profileIds];
      const message = `${playerNames} is about to be defaulted. Defaulting will set all game scores to 0 and it can't be undone.  Press \'OK\' to proceed or 'Cancel' to abort.`;
      const config = {
        width: '450px', height: '230px', data: {
          message: message, contentAreaHeight: 140
        }
      };
      const dialogRef = this.dialog.open(ConfirmationPopupComponent, config);
      dialogRef.afterClosed().subscribe(result => {
        if (result === 'ok') {
          this.proceedWithDefaulting(match, playerIndex);
        } else {
          // revert toggle
          const checkboxId = this.getCheckboxId(matchIndex, playerIndex);
          this.defaultCheckboxElementRefs.forEach((matCheckbox: MatCheckbox) => {
            if (matCheckbox.id === checkboxId) {
              matCheckbox.toggle();
            }
          });
        }
      });
    } else {
      this.proceedWithDefaulting(match, playerIndex);
    }
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
    // console.log('match.sideADefaulted', match.sideADefaulted);
    // console.log('match.sideBDefaulted', match.sideBDefaulted);
    const sideADefaulted: boolean = (playerIndex === 0) ? !match.sideADefaulted : match.sideADefaulted;
    const sideBDefaulted: boolean = (playerIndex === 1) ? !match.sideBDefaulted : match.sideBDefaulted;
    const updatedMatch: Match = {
      ...match,
      sideADefaulted: sideADefaulted,
      sideBDefaulted: sideBDefaulted,
      game1ScoreSideA: 0,
      game1ScoreSideB: 0,
      game2ScoreSideA: 0,
      game2ScoreSideB: 0,
      game3ScoreSideA: 0,
      game3ScoreSideB: 0,
      game4ScoreSideA: 0,
      game4ScoreSideB: 0,
      game5ScoreSideA: 0,
      game5ScoreSideB: 0,
      game6ScoreSideA: 0,
      game6ScoreSideB: 0,
      game7ScoreSideA: 0,
      game7ScoreSideB: 0
    };
    // console.log('updatedMatch.sideADefaulted', updatedMatch.sideADefaulted);
    // console.log('updatedMatch.sideBDefaulted', updatedMatch.sideBDefaulted);
    // console.log('updatedMatch', updatedMatch);
    this.updateMatch.emit(updatedMatch);
  }

  public onEnterScore(matchIndex: number) {
    this.enterMatchScore.emit({matchIndex: matchIndex});
  }
}
