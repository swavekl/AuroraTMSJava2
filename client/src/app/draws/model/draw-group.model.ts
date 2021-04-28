import {DrawItem} from './draw-item.model';

/**
 * Object representing a group of players in the draw
 */
export class DrawGroup {
  groupNum: number;
  drawItems: DrawItem [] = [];
}
