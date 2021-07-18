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

  // for round robin phase 0,
  // for single elimination - 64, 32, 16, 8 (quarter finals), 4 (semifinals), 2 (finals and 3rd/4th place)
  round: number;

  // map of player profile ids to their names
  profileIdToNameMap: any;

  public static getRoundShortName(round: number): string {
    let strRound = '';
    switch (round) {
      case 2:
        strRound = 'Final';
        break;
      case 4:
        strRound = 'Semi-Final';
        break;
      case 8:
        strRound = 'Quarter-Final';
        break;
      default:
        strRound = `Round of ${round}`;
        break;
    }
    return strRound;
  }
}
