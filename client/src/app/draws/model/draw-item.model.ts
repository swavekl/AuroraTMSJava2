/**
 * Represents a single player in a group draw
 */
import {DrawType} from './draw-type.enum';

export interface DrawItem {
  id: number;

  // id of the event for which this draw is made
  eventFk: number;

  // draw group number
  groupNum: number;

  // Players place in the draw for the group
  placeInGroup: number;

  drawType: DrawType;

  // id of the player (Okta) for fetching state, club etc.
  playerId: string;

  // list of conflicts - possibly null or list like 1, 2, 5 representing conflict types
  conflicts: string;

  // seed rating at a time of making the draws
  rating: number;

  // these values are added to enable easy showing of
  playerName: string;

  // state of US where player lives
  state: string;

  // name of the table tennis club where player plays
  clubName: string;
}
