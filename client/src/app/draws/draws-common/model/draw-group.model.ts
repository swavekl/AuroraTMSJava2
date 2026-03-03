import {DrawItem} from './draw-item.model';

/**
 * Object representing a group of players in the draw
 */
export class DrawGroup {
  groupNum: number;
  drawItems: DrawItem [] = [];
}

/**
 * Object representing a division in the draw, e.g. 1st, 2nd, 3rd etc.
 */
export class DrawDivision {
  divisionIdx: number;
  divisionName: string;
  groups: DrawGroup [] = [];
}
