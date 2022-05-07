import {PlayerResults} from './player-results';

export class EventResults {

  // is single elimination round result
  singleElimination: boolean;

  // if true this is doubles event
  doubles: boolean;

  // round robin group number
  groupNumber: number;

  // round of 16, 8 , quarterfinals etc.
  round: number;

  playerResultsList: PlayerResults [];
}
