import {Match} from './match.model';
import {DrawType} from '../../draws/model/draw-type.enum';

export class MatchCard {
  id: number;

  // event id to which this match belongs
  eventFk: number;

  // group number. if this is a card for group of matches it is 1, 2, 3 etc.
  // for single elimination phase it will be 0
  groupNum: number;

  // table numbers assigned to this match card could be one e.g. table number 4
  // or multiple if this is round robin phase 13,14
  assignedTable: string;

  // list of matches for this match card
  matches: Match[];

  // match for draw type
  drawType: DrawType;

  // best of 3, 5, 7 or 9 games per match in the main round (i.e. round robin)
  numberOfGames: number;

  // map of player profile ids to their names
  profileIdToNameMap: any;
}
