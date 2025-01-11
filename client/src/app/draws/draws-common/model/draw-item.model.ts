/**
 * Represents a single player in a group draw
 */
import {DrawType} from './draw-type.enum';
import {ConflictType} from './conflict-type.enum';

export interface DrawItem {
  id: number;

  // id of the event for which this draw is made
  eventFk: number;

  // draw group number
  groupNum: number;

  // for round robin phase 0,
  // for single elimination - 64, 32, 16, 8 (quarter finals), 4 (semifinals), 2 (finals and 3rd/4th place)
  round: number;

  // Players place in the draw for the group
  placeInGroup: number;

  // seed number of player/doubles team in single elimination round,
  // 0 in round robin round
  seSeedNumber: number;

  // bye number if this draw item represents a bye (e.g. 1, 2, 3 etc), 0 otherwise
  byeNum: number;

  // single elimination line number for preserving order of the bracket -
  singleElimLineNum: number;

  drawType: DrawType;

  // id of the player (Okta) for fetching state, club etc.
  playerId: string;

  // type of draw conflict if any
  conflictType: ConflictType;

  // seed rating at a time of making the draws
  rating: number;

  // these values are added to enable easy showing of
  playerName: string;

  // state of US where player lives
  state: string;

  // name of the table tennis club where player plays
  clubName: string;

  // tournament entry id for lookup of above geographical player information
  entryId: number;
}
