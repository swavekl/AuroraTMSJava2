import {DrawType} from '../../draws/draws-common/model/draw-type.enum';

/**
 * Abbreviated version of match card without matches
 */
export class MatchCardInfo {

  // match card id
  id: number;

  // match for draw type
  drawType: DrawType;

  // for round robin phase 0,
  // for single elimination - 64, 32, 16, 8 (quarter finals), 4 (semifinals), 2 (finals and 3rd/4th place)
  round: number;

  // group number e.g. 1, 2, 3 etc.
  groupNum: number;

  // table numbers assigned to this match card could be one e.g. table number 4
  // or multiple if this is round robin phase 13,14
  assignedTables: string;

  // fractional start time e.g. 9.5 = 9:30 am, 17.0 = 5:00 pm, -1.0 = To be Determined
  startTime: number;
}
