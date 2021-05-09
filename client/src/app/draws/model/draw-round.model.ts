import {DrawItem} from './draw-item.model';

export class DrawRound {
  // round of 32, 16, 8, 4 etc
  round: number;

  // items in this round
  drawItems: DrawItem [] = [];
}
