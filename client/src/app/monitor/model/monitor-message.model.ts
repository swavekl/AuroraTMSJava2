import {Match} from '../../matches/model/match.model';
import {MonitorMessageType} from './monitor-message-type';

/**
 * Message to update match status on the monitor display
 */
export interface MonitorMessage {
  // type of message
  messageType: MonitorMessageType;

  // current match status
  match: Match;

  playerAName: string;
  playerBName: string;
  playerAPartnerName: string;
  playerBPartnerName: string;

  doubles: boolean;

  // best of 3, 5, 7 or 9 games per match in the main round (i.e. round robin)
  numberOfGames: number;

  // most often 11 points but could be 21 or another
  pointsPerGame: number;

  // started or stopped (maybe prematurely)
  timeoutStarted: boolean;

  // player or doubles team requesting timeout
  timeoutRequester: string;

  // started or stopped
  warmupStarted: boolean;
}
