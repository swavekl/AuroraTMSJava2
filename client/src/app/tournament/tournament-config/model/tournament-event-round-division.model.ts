import {DrawMethod} from './draw-method.enum';

export class TournamentEventRoundDivision {

  // name of division e.g. Championship, Class AA, Class A etc.
  divisionName: string;

  // round robin options
  playersPerGroup: number = 4;

  // draw method
  drawMethod: DrawMethod = DrawMethod.SNAKE;

  // which players to pull into this division from previous round
  previousRoundPlayerRanking: number = 0;
  previousRoundPlayerRankingEnd: number = 0;

  // previous division index from which players will be drawn into this division
  previousDivisionIdx: number = 0;

  // number of tables per group
  numTablesPerGroup: number = 1;

  // points per game - 11 but sometimes 21
  pointsPerGame: number = 11;

  // best of 3, 5, 7 or 9 games per match
  numberOfGames: number = 5;

  // in single elimination round or if event is a single elimination only
  // number of games in rounds prior to quarter finals e.g. 5
  numberOfGamesSEPlayoffs: number = 5;

  // number of games in quarter, semi finals and 3rd/4th place matches
  numberOfGamesSEQuarterFinals: number = 5;
  numberOfGamesSESemiFinals: number = 5;
  numberOfGamesSEFinals: number = 5;

  // indicates if a match for 3rd adn 4th place is to be played
  play3rd4thPlace: boolean = false;

  // number of players to advance, 0, 1 or 2
  playersToAdvance: number = 1;

  // if this event advances player to another event or round - indicate if unrated players are to be advanced
  // typically not but in Open Singles they usually are
  advanceUnratedWinner: boolean = false;

  // number of players to seed directly into next round
  playersToSeed: number = 0;

}
