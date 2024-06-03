import {ChangeDetectorRef, Component, Input, OnChanges, OnInit, SimpleChange, SimpleChanges} from '@angular/core';
import {MonitorMessage} from '../model/monitor-message.model';
import {Match} from '../../matches/model/match.model';

@Component({
  selector: 'app-monitor-display',
  templateUrl: './monitor-display.component.html',
  styleUrls: ['./monitor-display.component.scss']
})
export class MonitorDisplayComponent implements OnInit, OnChanges {

  @Input()
  matchData: MonitorMessage;

  @Input()
  isConnected: boolean;

  // table number this screen is connected to
  @Input()
  tableNumber: number;

  // array so we can use iteration in the template
  games: number[];
  numGamesWonByA: number;
  numGamesWonByB: number;

  // current game score
  gameScoreSideA: number;
  gameScoreSideB: number;

  currentGameIndex: number;

  match: Match;

  constructor(protected cdr: ChangeDetectorRef) {
    this.games = Array(5);
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    const matchDataChange: SimpleChange = changes.matchData;
    if (matchDataChange != null) {
      const mm: MonitorMessage = matchDataChange.currentValue;
      if (mm != null) {
        // console.log('monitor display got changes', mm.numberOfGames);
        this.games = Array(mm.numberOfGames);

        this.match = mm.match;
        this.getScoreInGames(mm.numberOfGames, mm.pointsPerGame, this.match);
        this.currentGameIndex = this.getCurrentGameIndex(mm.numberOfGames, mm.pointsPerGame);
        this.getCurrentGameScore();
      }
    }
  }

  public getCurrentGameScore() {
    switch (this.currentGameIndex) {
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

  getScoreInGames (numberOfGames: number, pointsPerGame: number, match: Match) {
    let numGamesWonByA = 0;
    let numGamesWonByB = 0;
    for (let i = 0; i < numberOfGames; i++) {
      let playerAGameScore = 0;
      let playerBGameScore = 0;
      switch (i) {
        case 0:
          playerAGameScore = match.game1ScoreSideA;
          playerBGameScore = match.game1ScoreSideB;
          break;
        case 1:
          playerAGameScore = match.game2ScoreSideA;
          playerBGameScore = match.game2ScoreSideB;
          break;
        case 2:
          playerAGameScore = match.game3ScoreSideA;
          playerBGameScore = match.game3ScoreSideB;
          break;
        case 3:
          playerAGameScore = match.game4ScoreSideA;
          playerBGameScore = match.game4ScoreSideB;
          break;
        case 4:
          playerAGameScore = match.game5ScoreSideA;
          playerBGameScore = match.game5ScoreSideB;
          break;
        case 5:
          playerAGameScore = match.game6ScoreSideA;
          playerBGameScore = match.game6ScoreSideB;
          break;
        case 6:
          playerAGameScore = match.game7ScoreSideA;
          playerBGameScore = match.game7ScoreSideB;
          break;
      }

      if (playerAGameScore >= pointsPerGame && playerBGameScore < playerAGameScore) {
        numGamesWonByA++;
      } else if (playerBGameScore >= pointsPerGame && playerAGameScore < playerBGameScore) {
        numGamesWonByB++;
      }
    }

    this.numGamesWonByA = numGamesWonByA;
    this.numGamesWonByB = numGamesWonByB;
  }

  private getCurrentGameIndex(numberOfGames: number, pointsPerGame: number) {
    const totalGamesPlayed = this.numGamesWonByA + this.numGamesWonByB;
    const matchFinished = Match.isMatchFinished(this.match, numberOfGames, pointsPerGame);
    return matchFinished ? (totalGamesPlayed - 1) : Math.min(totalGamesPlayed, (numberOfGames - 1));
  }

  protected readonly isNaN = isNaN;
}
