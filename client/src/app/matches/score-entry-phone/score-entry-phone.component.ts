import {Component, Input, OnChanges, OnDestroy, OnInit, SimpleChange, SimpleChanges} from '@angular/core';
import {Match} from '../model/match.model';

@Component({
  selector: 'app-score-entry-phone',
  templateUrl: './score-entry-phone.component.html',
  styleUrls: ['./score-entry-phone.component.scss']
})
export class ScoreEntryPhoneComponent implements OnInit, OnChanges {

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
  }

  previousGame() {
    this.gameIndex--;
    console.log('go to previous game ', this.gameIndex);
  }

  hasPreviousGame() {
    return this.gameIndex > 0;
  }
  nextGame() {
    this.gameIndex++;
    console.log('go to next game ', this.gameIndex);
  }

  hasNextGame() {
    return this.gameIndex < this.numberOfGames;
  }

  isAddDisabled(playerIndex: number) {
    return false;
  }

  isSubtractDisabled(playerIndex: number) {
    return false;
  }

  subtractPoint(playerIndex: number) {
    if (playerIndex === 0) {
      this.gameScoreSideA--;
    } else {
      this.gameScoreSideB--;
    }
  }

  addPoint(playerIndex: number) {
    if (playerIndex === 0) {
      this.gameScoreSideA++;
    } else {
      this.gameScoreSideB++;
    }
  }

  onFocus($event: FocusEvent) {

  }

  onScoreValueChange($event: Event) {

  }

  setWinner(playerIndex: number) {
    console.log('setWinner', playerIndex);
    if (playerIndex === 0) {
      this.gameScoreSideA = 11;
      this.gameScoreSideB = 6;
    } else {
      this.gameScoreSideA = 6;
      this.gameScoreSideB = 11;
    }
  }
}
