import {MatchResult} from './match-result';

export class PlayerResults {
   letterCode: string;
   profileId: string;
   rank: number;
   rating: number;
   fullName: string;

   matchesWon: number;
   matchesLost: number;

   matchResults: MatchResult[];
}
