import {MatchResult} from './match-result';

export class PlayerResults {
  // player letter code e.g. A, B, C etc. in a group or match
  letterCode: string;

  // profile id of a player or doubles team
  profileId: string;

  // rank achieved in this group e.g. 1st, 2nd, etc.
  rank: number;

  // player rating or combined players rating for doubles team
  rating: number;

  // full name of names of doubles team members separated by slash
  fullName: string;

  // single elimination round seed number
  seSeedNumber: number;

  // single elimination round bye number
  byeNumber: number;

  // number of matches won in this group
  matchesWon: number;

  // number of matches lost in this group
  matchesLost: number;

  // match results against other players
  matchResults: MatchResult[];
}
