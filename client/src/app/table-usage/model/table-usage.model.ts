import {TableStatus} from './table-status';

/**
 * Class which keeps track of table usage at the tournament
 */
export class TableUsage {

  // unique id
  id: number;

  // tournament to which this table belongs
  tournamentFk: number;

  // table number
  tableNumber: number;

  // status
  tableStatus: TableStatus = TableStatus.Free;

  // id of match card assigned to play matches at this table or null
  matchCardFk: number;

  matchStartTime: Date;

  // completed matches and total matches on the match card
  completedMatches: number;
  totalMatches: number;

  // identifier of table or other device used to input scores during live score entry
  // to prevent using 2 devices on the same table
  scoreInputDeviceId: string;

}
