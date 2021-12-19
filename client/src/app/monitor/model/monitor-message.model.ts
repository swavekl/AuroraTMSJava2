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

  isDoubles: boolean;

  // best of 3, 5, 7 or 9 games per match in the main round (i.e. round robin)
  numberOfGames: number;

  // started or stopped (maybe prematurely)
  timeoutStarted: boolean;

  // player or doubles team requesting timeout
  timeoutRequester: string;

  // started or stopped
  warmupStarted: boolean;
}
