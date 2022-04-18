import {PlayerResults} from './player-results';

export class EventResults {

  // is single elimination round result
  singleElimination: boolean;

  // round robin group number
  groupNumber: number;

  // round of 16, 8 , quarterfinals etc.
  round: number;

  playerResultsList: PlayerResults [];
}
