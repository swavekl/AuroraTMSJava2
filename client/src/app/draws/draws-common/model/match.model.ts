import {DrawItem} from './draw-item.model';

/**
 * Represents a match between 2 opponents or doubles pair to be displayed on the single elimination draw
 */
export class Match {

  // draw items representing side A and B
  opponentA: DrawItem;
  opponentB: DrawItem;

  // time e.g. 10:30 am will be 10.5, 5:30 pm 17.5
  time: number;

  // table number on which match will take place
  tableNum: number;

  // results of the match if it is completed
  result: number [];

  // if true A won, if false B won
  opponentAWon: boolean;

  // if true the match template should show seed number
  showSeedNumber: boolean;

  // if true neither item in this match is draggable
  dragDisabled: boolean;

  getTooltipTextA() {
    return this.getTooltipTextFor(this.opponentA);
  }

  getTooltipTextB() {
    return this.getTooltipTextFor(this.opponentB);
  }

  private getTooltipTextFor(opponent: DrawItem) {
    if (!opponent || (opponent && opponent.byeNum === 0)) {
      return '';
    } else {
      return opponent.playerName + '<br/>' + opponent.state + ', ' + opponent.clubName;
    }
  }
}
