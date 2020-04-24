import {DateUtils} from '../../shared/date-utils';

export class Tournament {
  id: number;
  name: string;
  streetAddress: string;
  city: string;
  state: string;
  zipCode: string;
  startDate: Date;
  endDate: Date;

  // convert dates from string to date objects
  static convert(tournament: Tournament): Tournament {
    const dateUtils = new DateUtils();
    return {
      ...tournament,
      startDate: dateUtils.convertFromString(tournament.startDate),
      endDate: dateUtils.convertFromString(tournament.endDate)
    };
  }

  /**
   * Converts form values to Tournament object
   * @param formValues form values
   */
  static toTournament(formValues: any): Tournament {
    const dateUtils = new DateUtils();
    formValues.startDate = dateUtils.convertFromLocalToUTCDate(formValues.startDate);
    formValues.endDate = dateUtils.convertFromLocalToUTCDate(formValues.endDate);
    const returnValue: Tournament = new Tournament();
    Object.assign(returnValue, formValues);
    return returnValue;
  }


}
