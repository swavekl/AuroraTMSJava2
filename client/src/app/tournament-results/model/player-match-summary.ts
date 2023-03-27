/**
 * Results of individual match of a player.  Used for listing all matches played by a player
 * played in a tournament
 */
import {DrawType} from '../../draws/draws-common/model/draw-type.enum';

export class PlayerMatchSummary {
  // profile id of the opponent
  opponentProfileId: string;

  // membership id of the opponent
  opponentMembershipId: number;

  // full name of opponent
  opponentFullName: string;

  // opponent's rating
  opponentRating: number;

  // event name
  eventName: string;

  // event round
  round: number;

  // group number
  group: number;

  // match number on the match card
  matchNum: number;

  // true if doubles match
  doubles: boolean;

  // format of the whole event RR or SE
  eventFormat: DrawType;

  // compact result e.g. 6, 7, -5, 9
  compactMatchResult: string;

  // if true this is this player's win, if false opponent won
  matchWon: boolean;

  // number of exchanged points for a win or loss
  pointsExchanged: number;

  // day of tournament on which the match was played
  matchDay: number;
}
