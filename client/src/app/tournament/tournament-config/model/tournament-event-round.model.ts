import {TournamentEventRoundDivision} from './tournament-event-round-division.model';
/**
 * Information about each round used to generate draws
 */
export class TournamentEventRound {

  // e.g. Preliminary RR, Qualifying RR, Championship
  roundName: string;

  // round ordinal number so we can identify draws for this round 1, 2, 3
  ordinalNum: number;

  // single elimination or round robin
  singleElimination: boolean;

  // for multi day tournaments the round may start the next day
  day: number;

  // start time for this round
  startTime: number;

  // definitions of each division i.e. Championship, Class AA, Class A, B, C, D
  divisions: TournamentEventRoundDivision [];
}
