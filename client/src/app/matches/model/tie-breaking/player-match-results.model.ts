import {MatchStatus} from './match-status';

export class PlayerMatchResults {
  opponentCode: string;
  matchStatus: MatchStatus;
  gameScores: number[];
}
