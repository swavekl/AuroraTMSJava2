import {EventStatusCode} from './event-status-code.enum';

/**
 * Player status for check-in and communication
 */
export class PlayerStatus {
  // unique id
  id: number;

  // id of the player to whom this status refers
  playerProfileId: string;

  // tournament id for to which this status refers.  This is to support check-in for the whole tournament
  tournamentId: number;

  // day of tournament to which this status refers.  This is to support daily check-in but not for each event.
  // this should let us catch players who have not shown for the nth day.
  tournamentDay: number;

  // event id for to which this status refers.  This is to support check-in for each event
  eventId: number;

  // status code
  eventStatusCode: EventStatusCode;

  // reason for not playing - e.g. injured, change of plans, other
  reason: string;

  // estimated arrival time
  estimatedArrivalTime: string;

}
