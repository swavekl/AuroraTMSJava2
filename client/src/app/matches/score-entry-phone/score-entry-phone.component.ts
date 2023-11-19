import {
  AfterViewInit,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChange,
  SimpleChanges
} from '@angular/core';
import {Match} from '../model/match.model';
import {MatSnackBar} from '@angular/material/snack-bar';

@Component({
  selector: 'app-score-entry-phone',
  templateUrl: './score-entry-phone.component.html',
  styleUrls: ['./score-entry-phone.component.scss']
})
export class ScoreEntryPhoneComponent implements OnInit, OnChanges, AfterViewInit {

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

  @Input()
  public screenVisited: boolean = false;

  @Output()
  public saveMatch: EventEmitter<any> = new EventEmitter<any>();

  @Output()
  public cancelMatch: EventEmitter<any> = new EventEmitter<any>();

  // index of the game to show 0, 1, etc
  public gameToShowIndex: number;

  // array so we can use iteration in the template
  games: number [];

  // game scores that are edited
  gameScoreSideA: number;
  gameScoreSideB: number;

  dirty: boolean;

  constructor(private snackBar: MatSnackBar) {
    this.games = [];
    this.pointsPerGame = 11;
    this.gameToShowIndex = 0;
    this.numberOfGames = 5;
    this.gameScoreSideA = 0;
    this.gameScoreSideB = 0;
    this.dirty = false;
  }

  ngOnInit(): void {
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
    this.dirty = true;
  }

  addPoint(playerIndex: number) {
    if (!this.isWinnerSelected()) {
      this.setWinner(playerIndex);
    } else {
      if (playerIndex === 0) {
        this.gameScoreSideA++;
        if (this.gameScoreSideA >= this.pointsPerGame + 1) {
          this.gameScoreSideB = this.gameScoreSideA - 2;
        }
      } else {
        this.gameScoreSideB++;
        if (this.gameScoreSideB >= this.pointsPerGame + 1) {
          this.gameScoreSideA = this.gameScoreSideB - 2;
        }
      }
      this.dirty = true;
    }
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

  ngAfterViewInit(): void {
    if (this.screenVisited === false) {
      const statusText = `Touch name of player who won the game first and then adjust the opponent's score with + / - buttons.`;
      this.snackBar.open(statusText, "Close", {
        verticalPosition: 'top'
      });
    }
  }
}
