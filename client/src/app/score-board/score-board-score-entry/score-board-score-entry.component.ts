import {Component, EventEmitter, Input, Output, SimpleChange, SimpleChanges} from '@angular/core';
import {Match} from '../../matches/model/match.model';
import {finalize, tap} from 'rxjs/operators';
import {takeWhile, timer} from 'rxjs';

@Component({
  selector: 'app-score-board-score-entry',
  templateUrl: './score-board-score-entry.component.html',
  styleUrl: './score-board-score-entry.component.scss'
})
export class ScoreBoardScoreEntryComponent {
  @Input()
  public match: Match;

  @Input()
  public playerAName: string;

  @Input()
  public playerBName: string;

  @Input()
  public pointsPerGame: number;

  @Input()
  public numberOfGames: number;

  @Input()
  public doubles: boolean;

  @Output()
  public saveMatch: EventEmitter<any> = new EventEmitter<any>();

  @Output()
  public cancelMatch: EventEmitter<any> = new EventEmitter<any>();

  @Output()
  public timerEvent: EventEmitter<any> = new EventEmitter<any>();

  // index of the game to show 0, 1, etc
  public gameToShowIndex: number;

  // array so we can use iteration in the template
  games: number [];

  // game scores that are edited
  gameScoreSideA: number;
  gameScoreSideB: number;

  dirty: boolean;

  timerValue: number = 0;
  timerRunning: boolean = false;

  // timeout started for player A or B
  timeoutForPlayer: string = null;

  constructor() {
    this.games = [];
    this.pointsPerGame = 11;
    this.gameToShowIndex = 0;
    this.numberOfGames = 5;
    this.gameScoreSideA = 0;
    this.gameScoreSideB = 0;
    this.dirty = false;
  }

  ngOnChanges(changes: SimpleChanges): void {
    const numberOfGamesChanges: SimpleChange = changes.numberOfGames;
    if (numberOfGamesChanges) {
      const numberOfGames = numberOfGamesChanges.currentValue;
      if (numberOfGames) {
        this.games = Array(numberOfGames);
      }
    }
    const matchChanges: SimpleChange = changes.match;
    if (matchChanges) {
      const match: Match = matchChanges.currentValue;
      if (match) {
        this.gameToShowIndex = this.getNextGameIndex(this.match);
        this.getGameScore();
      }
    }
  }

  previousGame() {
    this.saveGameScore();
    if (this.dirty) {
      this.saveMatch.emit({updatedMatch: this.match, backToMatchCard: false});
      this.dirty = false;
    }
    this.gameToShowIndex--;
    this.getGameScore();
  }

  hasPreviousGame() {
    return this.gameToShowIndex > 0;
  }

  nextGame() {
    this.saveGameScore();
    if (this.dirty) {
      this.saveMatch.emit({updatedMatch: this.match, backToMatchCard: false});
      this.dirty = false;
    }
    this.gameToShowIndex++;
    this.getGameScore();
  }

  hasNextGame() {
    let hasNextGame: boolean;
    const matchFinished = Match.isMatchFinished(this.match, this.numberOfGames, this.pointsPerGame);
    if (matchFinished) {
      // console.log('matchFinished', matchFinished);
      // don't allow to advance past the last entered match
      // they need to change prior games to do that
      const nextBlankGameIndex = Match.nextNotEnteredGameIndex(this.match, this.numberOfGames);
      hasNextGame = this.gameToShowIndex < (nextBlankGameIndex - 1);
    } else {
      // let them enter games until match is fully entered
      hasNextGame = this.gameToShowIndex < (this.numberOfGames - 1);
    }
    // console.log('in hasNextGame', hasNextGame);
    return hasNextGame;
  }

  private isWinnerSelected(): boolean {
    return !(this.gameScoreSideB === 0 && this.gameScoreSideA === 0);
  }

  isSubtractDisabled() {
    return !this.isWinnerSelected();
  }

  subtractPoint(playerIndex: number) {
    if (playerIndex === 0) {
      this.gameScoreSideA -= (this.gameScoreSideA > 0) ? 1 : 0;
    } else {
      this.gameScoreSideB -= (this.gameScoreSideB > 0) ? 1 : 0;
    }
    this.saveGameScore();
    this.saveMatch.emit({updatedMatch: this.match, backToMatchCard: false});
    this.dirty = true;
  }

  addPoint(playerIndex: number) {
    // if (!this.isWinnerSelected()) {
    //   this.setWinner(playerIndex);
    // } else {
    // }
    if (playerIndex === 0) {
      this.gameScoreSideA++;
      // if (this.gameScoreSideA >= this.pointsPerGame + 1) {
      //   this.gameScoreSideB = this.gameScoreSideA - 2;
      // }
    } else {
      this.gameScoreSideB++;
      // if (this.gameScoreSideB >= this.pointsPerGame + 1) {
      //   this.gameScoreSideA = this.gameScoreSideB - 2;
      // }
    }
    this.saveGameScore();
    this.saveMatch.emit({updatedMatch: this.match, backToMatchCard: false});
    this.dirty = true;
  }

  setWinner(playerIndex: number) {
    if (playerIndex === 0) {
      this.gameScoreSideA = this.pointsPerGame;
      this.gameScoreSideB = this.pointsPerGame - 5;
    } else {
      this.gameScoreSideA = this.pointsPerGame - 5;
      this.gameScoreSideB = this.pointsPerGame;
    }
    this.saveGameScore();
    this.dirty = true;
  }

  private getNextGameIndex(match: Match): number {
    const matchFinished = Match.isMatchFinished(match, this.numberOfGames, this.pointsPerGame);
    // console.log('matchFinished', matchFinished);
    if (matchFinished) {
      // show first game - maybe they want to correct it
      return 0;
    } else {
      return Match.nextNotEnteredGameIndex(match, this.numberOfGames);
    }
  }

  /**
   * Sets current game score
   */
  getGameScore() {
    switch (this.gameToShowIndex) {
      case 0:
        this.gameScoreSideA = this.match.game1ScoreSideA;
        this.gameScoreSideB = this.match.game1ScoreSideB;
        break;
      case 1:
        this.gameScoreSideA = this.match.game2ScoreSideA;
        this.gameScoreSideB = this.match.game2ScoreSideB;
        break;
      case 2:
        this.gameScoreSideA = this.match.game3ScoreSideA;
        this.gameScoreSideB = this.match.game3ScoreSideB;
        break;
      case 3:
        this.gameScoreSideA = this.match.game4ScoreSideA;
        this.gameScoreSideB = this.match.game4ScoreSideB;
        break;
      case 4:
        this.gameScoreSideA = this.match.game5ScoreSideA;
        this.gameScoreSideB = this.match.game5ScoreSideB;
        break;
      case 5:
        this.gameScoreSideA = this.match.game6ScoreSideA;
        this.gameScoreSideB = this.match.game6ScoreSideB;
        break;
      case 6:
        this.gameScoreSideA = this.match.game7ScoreSideA;
        this.gameScoreSideB = this.match.game7ScoreSideB;
        break;
      default:
        this.gameScoreSideA = 0;
        this.gameScoreSideB = 0;
        break;
    }
  }

  /**
   * Saves currently displayed game score
   */
  saveGameScore() {
    switch (this.gameToShowIndex) {
      case 0:
        this.match = {...this.match,
          game1ScoreSideA: this.gameScoreSideA,
          game1ScoreSideB: this.gameScoreSideB
        };
        break;
      case 1:
        this.match = {...this.match,
          game2ScoreSideA: this.gameScoreSideA,
          game2ScoreSideB: this.gameScoreSideB
        };
        break;
      case 2:
        this.match = {...this.match,
          game3ScoreSideA: this.gameScoreSideA,
          game3ScoreSideB: this.gameScoreSideB
        };
        break;
      case 3:
        this.match = {...this.match,
          game4ScoreSideA: this.gameScoreSideA,
          game4ScoreSideB: this.gameScoreSideB
        };
        break;
      case 4:
        this.match = {...this.match,
          game5ScoreSideA: this.gameScoreSideA,
          game5ScoreSideB: this.gameScoreSideB
        };
        break;
      case 5:
        this.match = {...this.match,
          game6ScoreSideA: this.gameScoreSideA,
          game6ScoreSideB: this.gameScoreSideB
        };
        break;
      case 6:
        this.match = {...this.match,
          game7ScoreSideA: this.gameScoreSideA,
          game7ScoreSideB: this.gameScoreSideB
        };
        break;
    }
  }

  cancel() {
    this.cancelMatch.emit(null);
  }

  reset() {
    this.dirty = (this.gameScoreSideA > 0 || this.gameScoreSideB > 0);
    this.gameScoreSideA = 0;
    this.gameScoreSideB = 0;
    this.saveGameScore();
    this.saveMatch.emit({updatedMatch: this.match, backToMatchCard: false});
  }

  save() {
    if (this.isScoreValid()) {
      this.saveGameScore();
      this.saveMatch.emit({updatedMatch: this.match, backToMatchCard: true});
      this.dirty = false;
    }
  }

  isScoreValid(): boolean {
    if (this.gameScoreSideA > this.gameScoreSideB) {
      return (this.gameScoreSideA === this.pointsPerGame)
        ? ((this.gameScoreSideA - this.gameScoreSideB) >= 2)
        : ((this.gameScoreSideA - this.gameScoreSideB) === 2);
    } else if (this.gameScoreSideB > this.gameScoreSideA) {
      return (this.gameScoreSideB === this.pointsPerGame)
        ? ((this.gameScoreSideB - this.gameScoreSideA) >= 2)
        : ((this.gameScoreSideB - this.gameScoreSideA) === 2);
    } else {
      return false;
    }
  }

  isMatchWinner(profileId: string): boolean {
    return (this.match) ? Match.isMatchWinner(profileId, this.match, this.numberOfGames, this.pointsPerGame) : false;
  }

  getDoublesPlayerName (playerNames: string, index: number): string {
    const playerNamesArray = playerNames.split('/');
    return playerNamesArray[index].trim();
  }

  startTimer(duration: number) {
    if (!this.timerRunning) {
      this.timerRunning = true;
      this.timerValue = duration;
      timer(1000, 1000)
        .pipe(
          takeWhile( () => this.timerValue > 0 ),
          tap(() => this.timerValue--),
          finalize(() => {
            this.timerRunning = false;
            this.timerValue = 0;
          })
        ).subscribe( );
    }
  }

  stopTimer() {
    if (this.timerRunning) {
      this.timerValue = 0;
    }
  }

  startWarmup() {
    this.startTimer(120);
    this.timerEvent.emit({action: 'startWarmup', timeoutForPlayer: null, updatedMatch: this.match});
  }

  stopWarmup() {
    this.stopTimer();
    this.timerEvent.emit({action: 'stopWarmup', timeoutForPlayer: null, updatedMatch: this.match});
  }

  canStartWarmup() {
    if (!this.timerRunning) {
      return this.isMatchNotStarted();
    } else {
      return false;
    }
  }

  isMatchNotStarted () {
    if (this.match != null) {
      return (this.match.game1ScoreSideA === 0 && this.match.game1ScoreSideB === 0) &&
        (this.match.game2ScoreSideA === 0 && this.match.game2ScoreSideB === 0) &&
        (this.match.game3ScoreSideA === 0 && this.match.game3ScoreSideB === 0) &&
        (this.match.game4ScoreSideA === 0 && this.match.game4ScoreSideB === 0) &&
        (this.match.game5ScoreSideA === 0 && this.match.game5ScoreSideB === 0) &&
        (this.match.game6ScoreSideA === 0 && this.match.game6ScoreSideB === 0) &&
        (this.match.game7ScoreSideA === 0 && this.match.game7ScoreSideB === 0);
    } else {
      return false;
    }
  }

  canStopWarmup() {
    return this.canStartWarmup() && this.timerRunning;
  }

  startTimeout(playerLetter: string) {
    if (!this.timerRunning) {
      const sideATimeoutTaken = (playerLetter === 'A') || this.match.sideATimeoutTaken;
      const sideBTimeoutTaken = (playerLetter === 'B') || this.match.sideBTimeoutTaken;
      this.timeoutForPlayer = playerLetter;
      this.match = {
        ...this.match,
        sideATimeoutTaken: sideATimeoutTaken,
        sideBTimeoutTaken: sideBTimeoutTaken
      };
      this.timerEvent.emit({action: 'startTimeout', timeoutForPlayer: this.timeoutForPlayer, updatedMatch: this.match});
      this.startTimer(60);
    }
  }

  stopTimeout() {
    if (this.timerRunning) {
      this.stopTimer();
      this.timerEvent.emit({action: 'stopTimeout', timeoutForPlayer: this.timeoutForPlayer, updatedMatch: this.match});
    }
  }

  canStartTimeout(playerLetter: string) {
    if (this.match != null && !this.timerRunning) {
      const matchStarted = !this.isMatchNotStarted();
      if (playerLetter === 'A') {
        return matchStarted && this.match.sideATimeoutTaken === false;
      } else if (playerLetter === 'B') {
        return matchStarted && this.match.sideBTimeoutTaken === false;
      }
    } else {
      return false;
    }
  }

  canStopTimeout() {
    if (this.timerRunning) {
      return this.match?.sideATimeoutTaken || this.match?.sideBTimeoutTaken;
    } else {
      return false;
    }
  }
}
