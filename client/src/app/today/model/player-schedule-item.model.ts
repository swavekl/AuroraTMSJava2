import {PlayerDetail} from './player-detail.model';

/**
 * Information about the round-robin round group opponents
 * or a single match from a single elimination round
 * This is data mostly from the match cards
 */
export class PlayerScheduleItem {

  // day 1 of tournament, day 2 etc.
  day: number;

  // event start time or individual match start time, 10.5 is 10:30
  startTime: number;

  // name of the event
  eventName: string;

  // event id
  eventId: number;

  // 0 for RR, 16, 8, 4, 2 etc for single elimination phase
  round: number;

  // group number for RR phase
  group: number;

  // comma separated, if more than one
  assignedTables: string;

  // id of a match card corresponding to this
  matchCardId: number;

  // details of each player in this item (group or match)
  playerDetails: PlayerDetail[];
}
