import {PlayerMatchResults} from './player-match-results.model';

export class PlayerTieBreakingInfo {

  playerProfileId: string;
  playerCode: string;
  rank: number;

  matchPoints: number;

  gamesWon: number;
  gamesLost: number;

  pointsWon: number;
  pointsLost: number;

  allPlayerMatchResults: PlayerMatchResults[];

}
