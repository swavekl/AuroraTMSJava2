import {PrizeInfo} from './prize-info.model';

/**
 * Additional information which will not be queryable.  This will allow us to add new configuration data in the future easily
 */
export class TournamentEventConfiguration {

  // prize money information

  prizeInfoList: PrizeInfo[] = [];

  // final player rankings in the event 1st place x, 2nd place y etc.
  finalPlayerRankings = {};

}
