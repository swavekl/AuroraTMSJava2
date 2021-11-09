import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChange, SimpleChanges} from '@angular/core';
import {Match} from '../model/match.model';

@Component({
  selector: 'app-score-entry-phone',
  templateUrl: './score-entry-phone.component.html',
  styleUrls: ['./score-entry-phone.component.scss']
})
export class ScoreEntryPhoneComponent implements OnInit, OnChanges {

  @Input()
  public match: Match;

  // copy of this match with games saved
  private matchCopy: Match;

  @Input()
  public playerAName: string;
  @Input()
  public playerBName: string;

  @Input()
  public pointsPerGame: number;

  @Input()
  public numberOfGames: number;

  @Output()
  public saveMatch: EventEmitter<Match> = new EventEmitter<Match>();

  @Output()
  public cancelMatch: EventEmitter<any> = new EventEmitter<any>();


  public gameIndex: number;

  // array so we can use iteration in the template
  games: number [];

  gameScoreSideA: number;
  gameScoreSideB: number;

  constructor() {
    this.games = [];
    this.pointsPerGame = 11;
    this.gameIndex = 0;
    this.numberOfGames = 5;
    this.gameScoreSideA = 0;
    this.gameScoreSideB = 0;
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
        this.matchCopy = JSON.parse(JSON.stringify(match));
        this.gameIndex = this.getNextGameIndex(match);
        this.getGameScore();
      }
    }
  }

  previousGame() {
    this.saveGameScore();
    this.gameIndex--;
    this.getGameScore();
  }

  hasPreviousGame() {
    return this.gameIndex > 0;
  }

  nextGame() {
    this.saveGameScore();
    this.gameIndex++;
    this.getGameScore();
  }

  hasNextGame() {
    return this.gameIndex < this.numberOfGames;
  }

  isAddDisabled(playerIndex: number) {
    return (this.gameScoreSideB === 0 && this.gameScoreSideA === 0);
  }

  isSubtractDisabled(playerIndex: number) {
    return (this.gameScoreSideB === 0 && this.gameScoreSideA === 0);
  }

  subtractPoint(playerIndex: number) {
    if (playerIndex === 0) {
      this.gameScoreSideA -= (this.gameScoreSideA > 0) ? 1 : 0;
    } else {
      this.gameScoreSideB -= (this.gameScoreSideB > 0) ? 1 : 0;
    }
  }

  addPoint(playerIndex: number) {
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
  }

  setWinner(playerIndex: number) {
    if (playerIndex === 0) {
      this.gameScoreSideA = this.pointsPerGame;
      this.gameScoreSideB = this.pointsPerGame - 5;
    } else {
      this.gameScoreSideA = this.pointsPerGame - 5;
      this.gameScoreSideB = this.pointsPerGame;
    }
  }

  private getNextGameIndex(match: Match): number {
    if (match.game1ScoreSideA === 0 && match.game1ScoreSideB === 0) {
      return 0;
    } else if (match.game2ScoreSideA === 0 && match.game2ScoreSideB === 0) {
      return 1;
    } else if (match.game3ScoreSideA === 0 && match.game3ScoreSideB === 0) {
      return 2;
    } else if (match.game4ScoreSideA === 0 && match.game4ScoreSideB === 0) {
      return 3;
    } else if (match.game5ScoreSideA === 0 && match.game5ScoreSideB === 0) {
      return 4;
    } else if (match.game6ScoreSideA === 0 && match.game6ScoreSideB === 0) {
      return 5;
    } else if (match.game7ScoreSideA === 0 && match.game7ScoreSideB === 0) {
      return 6;
    }
  }

  /**
   * Sets current game score
   */
  getGameScore() {
    switch (this.gameIndex) {
      case 0:
        this.gameScoreSideA = this.matchCopy.game1ScoreSideA;
        this.gameScoreSideB = this.matchCopy.game1ScoreSideB;
        break;
      case 1:
        this.gameScoreSideA = this.matchCopy.game2ScoreSideA;
        this.gameScoreSideB = this.matchCopy.game2ScoreSideB;
        break;
      case 2:
        this.gameScoreSideA = this.matchCopy.game3ScoreSideA;
        this.gameScoreSideB = this.matchCopy.game3ScoreSideB;
        break;
      case 3:
        this.gameScoreSideA = this.matchCopy.game4ScoreSideA;
        this.gameScoreSideB = this.matchCopy.game4ScoreSideB;
        break;
      case 4:
        this.gameScoreSideA = this.matchCopy.game5ScoreSideA;
        this.gameScoreSideB = this.matchCopy.game5ScoreSideB;
        break;
      case 5:
        this.gameScoreSideA = this.matchCopy.game6ScoreSideA;
        this.gameScoreSideB = this.matchCopy.game6ScoreSideB;
        break;
      case 6:
        this.gameScoreSideA = this.matchCopy.game7ScoreSideA;
        this.gameScoreSideB = this.matchCopy.game7ScoreSideB;
        break;
      default:
        this.gameScoreSideA = 0;
        this.gameScoreSideB = 0;
        break;
    }
  }

  /**
   * Saves currently displyed game score
   */
  saveGameScore() {
    switch (this.gameIndex) {
      case 0:
        this.matchCopy.game1ScoreSideA = this.gameScoreSideA;
        this.matchCopy.game1ScoreSideB = this.gameScoreSideB;
        break;
      case 1:
        this.matchCopy.game2ScoreSideA = this.gameScoreSideA;
        this.matchCopy.game2ScoreSideB = this.gameScoreSideB;
        break;
      case 2:
        this.matchCopy.game3ScoreSideA = this.gameScoreSideA;
        this.matchCopy.game3ScoreSideB = this.gameScoreSideB;
        break;
      case 3:
        this.matchCopy.game4ScoreSideA = this.gameScoreSideA;
        this.matchCopy.game4ScoreSideB = this.gameScoreSideB;
        break;
      case 4:
        this.matchCopy.game5ScoreSideA = this.gameScoreSideA;
        this.matchCopy.game5ScoreSideB = this.gameScoreSideB;
        break;
      case 5:
        this.matchCopy.game6ScoreSideA = this.gameScoreSideA;
        this.matchCopy.game6ScoreSideB = this.gameScoreSideB;
        break;
      case 6:
        this.matchCopy.game7ScoreSideA = this.gameScoreSideA;
        this.matchCopy.game7ScoreSideB = this.gameScoreSideB;
        break;
    }
  }

  cancel() {
    this.cancelMatch.emit(null);
  }

  reset() {
    this.gameScoreSideA = 0;
    this.gameScoreSideB = 0;
  }

  save() {
    if (this.isScoreValid()) {
      this.saveGameScore();
      this.saveMatch.emit(this.matchCopy);
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

  isGameWinner(playerIndex: number): boolean {
    if (this.isScoreValid()) {
      if (playerIndex === 0) {
        return this.gameScoreSideA > this.gameScoreSideB;
      } else {
        return this.gameScoreSideB > this.gameScoreSideA;
      }
    }
    return false;
  }
}
