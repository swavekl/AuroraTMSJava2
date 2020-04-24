import * as moment from 'moment';

export class DateUtils {

  constructor() {

  }

  convertFromString(strDate: any): Date {
    let result: Date = null;
    if (strDate != null) {
      // @ts-ignore
      result = new Date(strDate);
    }
    return result;
  }

  /**
   * We store dates in UTC so they are time zone independent.
   * Now convert to local time zone so date picker displays them correctly.
   */
  convertFromUTCToLocalDate(utcDate: Date): Date {
    // incoming string is in UTC
//    console.log ('UTC time: ', utcDate);
    const utcMoment = moment.utc(utcDate, moment.ISO_8601);
    // convert to local time zone
    const localMoment = moment([utcMoment.year(), utcMoment.month(), utcMoment.date(), 0, 0, 0]).local();
    const localDate = localMoment.toDate();
    return localDate;
  }

  convertFromLocalToUTCDate(localDate: Date): Date {
//    console.log ('localDate ', localDate);

    const localMoment = moment(localDate);
    const utcMoment = moment([localMoment.year(), localMoment.month(), localMoment.date(), 0, 0, 0]).utc();
    const utcDate = utcMoment.toDate();
//    console.log ('utcDate ', utcDate);
    return utcDate;
  }
}
