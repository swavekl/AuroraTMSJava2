import {DateUtils} from '../../shared/date-utils';
import {CheckInType} from './check-in-type.enum';

export class TournamentInfo {
  id: number;
  name: string;
  venueName: string;
  streetAddress: string;
  city: string;
  state: string;
  zipCode: string;
  startDate: Date;
  endDate: Date;
  tournamentType: String;
  starLevel: number;
  numEntries: number;
  numEventEntries: number;
  maxNumEventEntries: number;

  tournamentDirectorName: string;
  tournamentDirectorPhone: string;
  tournamentDirectorEmail: string;

  // path to graphic file with tournament logo
  logo: string;
  // daily or for each event
  checkInType: CheckInType;

  // convert dates from string to date objects
  static convert(tournamentInfo: TournamentInfo): TournamentInfo {
    const dateUtils = new DateUtils();
    return {
      ...tournamentInfo,
      startDate: dateUtils.convertFromString(tournamentInfo.startDate),
      endDate: dateUtils.convertFromString(tournamentInfo.endDate)
    };
  }
}
