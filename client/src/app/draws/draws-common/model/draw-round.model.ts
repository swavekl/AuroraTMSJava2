import {DrawItem} from './draw-item.model';

/**
 * Represents a round of single elimination event draw items which coprose a match or a bye
 */
export class DrawRound {
  // round of 32, 16, 8, 4 etc
  round: number;

  // items in this round
  drawItems: DrawItem [] = [];
}

// simulates ngttRound
export interface SERound {
  type: 'Winnerbracket' | 'Loserbracket' | 'Final';
  matches: any[];
}

export class DropListData {
  firstRound: number;
  dropListDrawItems: DrawItem[] = [];
}

export class SEDrawDivision {
  divisionIdx: number;
  divisionName: string;
  singleEliminationRounds: DrawRound [] = [];
  seRounds: SERound [] = [];
  dropListData: DropListData;
  roundNumbers: number [] = [];
  svgData: any;
}
