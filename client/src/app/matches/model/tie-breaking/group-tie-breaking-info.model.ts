import {PlayerTieBreakingInfo} from './player-tie-breaking-info.model';

export class GroupTieBreakingInfo {

  // map of player id to full name
  profileIdToNameMap: any;

  // main matrix holding final results for all players
  playerTieBreakingInfoList: PlayerTieBreakingInfo[];

  // maps a string indicating the order in which tie breaks are resolved
  // to a subset of matrix used to do the tie breaking
  nwayTieBreakingInfosMap: Map<string, PlayerTieBreakingInfo[]>;
}
