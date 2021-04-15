import {DateUtils} from '../../shared/date-utils';

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
