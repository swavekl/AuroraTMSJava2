import {Match} from './match.model';
import {DrawType} from '../../draws/model/draw-type.enum';

export class MatchCard {
  id: number;

  // event id to which this match belongs
  eventFk: number;

  // group number. if this is a card for group of matches it is 1, 2, 3 etc.
  // for single elimination phase it will be 0
  groupNum: number;

  // for single elimination match cards this is the prior round (i.e. round robin) group number from which this player came
  playerAGroupNum: number;
  playerBGroupNum: number;

  // table numbers assigned to this match card could be one e.g. table number 4
  // or multiple if this is round robin phase 13,14
  assignedTables: string;

  // list of matches for this match card
  matches: Match[];

  // match for draw type
  drawType: DrawType;

  // best of 3, 5, 7 or 9 games per match in the main round (i.e. round robin)
  numberOfGames: number;

  // for round robin phase 0,
  // for single elimination - 64, 32, 16, 8 (quarter finals), 4 (semifinals), 2 (finals and 3rd/4th place)
  round: number;

  // day of the tournament on which this event is played 1, 2, 3 etc
  day: number;

  // fractional start time e.g. 9.5 = 9:30 am, 17.0 = 5:00 pm, -1.0 = To be Determined
  startTime: number;

  // total scheduled duration in minutes of all matches on this match card.
  // This is assuming that they will be played on 1 table only, if played on 2 tables divide that by 2, if on 3 dividde by
  // so if played on 2 tables it will
  duration: number;

  // String representing player rankings
  playerRankings: any;

  // map of player profile ids to their names
  profileIdToNameMap: any;

  /**
   * gets full match name so we name them consistently throughout the project
   * @param eventName
   * @param drawType
   * @param round
   * @param groupNum
   */
  public static getFullMatchName (eventName: string, drawType: DrawType, round: number, groupNum: number): string {
    let matchIdentifierText = '';
    if (drawType === 'ROUND_ROBIN') {
      matchIdentifierText = `${eventName} R.R. Group ${groupNum}`;
    } else {
      const roundName = this.getMatchName(round, groupNum);
      matchIdentifierText = `${eventName} ${roundName}`;
    }
    return matchIdentifierText;
  }

  public static getMatchName (round: number, groupNum: number): string {
    const roundName = this.getRoundName(round, groupNum);
    const matchNumber = (round === 2) ? '' : ` M ${groupNum}`;
    return roundName + matchNumber;
  }

  public static getRoundName(round: number, groupNum: number): string {
    let strRound = '';
    switch (round) {
      case 2:
        strRound = (groupNum === 1) ? 'Final' : '3rd & 4th Place';
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

  public static getRoundAbbreviatedName(round: number): string {
    let strRound = '';
    switch (round) {
      case 2:
        strRound = 'Final';
        break;
      case 4:
        strRound = 'S.F.';
        break;
      case 8:
        strRound = 'Q.F.';
        break;
      default:
        strRound = `R. of ${round}`;
        break;
    }
    return strRound;
  }
}
