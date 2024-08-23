import {Component, Input, OnChanges, SimpleChange, SimpleChanges} from '@angular/core';
import {Match} from '../model/match.model';

/**
 * Table showing player names or letter codes, individual games score and game score for the match
 */
@Component({
  selector: 'app-match-score-table',
  templateUrl: './score-table.component.html',
  styleUrl: './score-table.component.scss'
})
export class ScoreTableComponent implements OnChanges {
  @Input()
  match: Match;

  @Input()
  public numberOfGames: number;

  @Input()
  public doubles: boolean;

  @Input()
  public currentGameIndex: number;

  @Input()
  public playerAName: string;

  @Input()
  public playerBName: string;

  @Input()
  public pointsPerGame: number;

  @Input()
  public showPlayerNames: boolean;

  // array so we can use iteration in the template
  games: number [];

  // score in games for player A and B
  public playerAGames: number;
  public playerBGames: number;

  constructor() {
    this.games = [];
    this.pointsPerGame = 11;
    this.numberOfGames = 5;
    this.currentGameIndex = 0;
    this.playerAGames = 0;
    this.playerBGames = 0;
  }

  ngOnChanges(changes: SimpleChanges): void {
    const matchSimpleChange: SimpleChange = changes.match;
    if (matchSimpleChange != null) {
      const match = matchSimpleChange.currentValue;
      if (match != null && this.numberOfGames != 0 && this.pointsPerGame != 0) {
        const scoreInGames = Match.getScoreInGames(this.numberOfGames, this.pointsPerGame, this.match);
        this.playerAGames = scoreInGames.playerAGames;
        this.playerBGames = scoreInGames.playerBGames;
      }
    }

    // make an array of games so we can iterate it to produce the table
    const numberOfGamesChanges: SimpleChange = changes.numberOfGames;
    if (numberOfGamesChanges) {
      const numberOfGames = numberOfGamesChanges.currentValue;
      if (numberOfGames) {
        this.games = Array(numberOfGames);
      }
    }
  }

  getDoublesPlayerName(playerNames: string, index: number): string {
    const playerNamesArray = playerNames.split('/');
    return playerNamesArray[index].trim();
  }

  isCurrentGame(gameIndex: number): boolean {
    return gameIndex === this.currentGameIndex;
  }
}
