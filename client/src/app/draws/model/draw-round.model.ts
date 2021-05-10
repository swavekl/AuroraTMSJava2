import {DrawItem} from './draw-item.model';

/**
 * Represents a round of single elimination event draw items which coprose a match or a bye
 */
export class DrawRound {
  // round of 32, 16, 8, 4 etc
  round: number;

  // items in this round
  drawItems: DrawItem [] = [];

  getRoundName (): string {
    switch (this.round) {
      case 2:
        return 'Finals';
      case 4:
        return 'Semi-Finals';
      case 8:
        return 'Quarter-Finals';
      default:
        return 'Round of ' + this.round;
    }
  }
}
