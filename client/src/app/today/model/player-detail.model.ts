import {EventStatusCode} from './event-status-code.enum';

/**
 * Player detail for this event
 */
export class PlayerDetail {
  // 0 for unrated, otherwise rating
  rating: number;

  // if true this is estimated rating
  estimated: boolean;

  // A, B, C etc.
  playerCode: string;

  // last name, first name
  playerFullName: string;

  // will play or not
  statusCode: EventStatusCode;

  // ETA if late arrival
  estimatedArrivalTime: string;

  // Reason if will not play
  reason: string;
}
