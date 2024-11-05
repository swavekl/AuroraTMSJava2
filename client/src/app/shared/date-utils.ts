import moment from 'moment';
import {StartTimePipe} from './pipes/start-time.pipe';

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

  getDayAsString (startDate: Date, day: number) {
    const localMoment = moment(startDate).add((day - 1), 'day');
    return localMoment.format('dddd');
  }

  getTimeAsString(startDate: Date, startTime: number) {
    const fractionOfHour = (startTime % 1).toFixed(2);
    // @ts-ignore
    const minutes = 60 * fractionOfHour;
    const hours = Math.floor(startTime);
    const localMoment = moment(startDate).hours(hours).minutes(minutes).seconds(0).milliseconds(0);
    return localMoment.format('LT');
  }

  daysBetweenDates (startDate: Date | string, endDate: Date | string): number {
    let diff = 0;
    // if (startDate instanceof String && endDate instanceof String) {
    //   const mStartDate = moment(this.convertFromString(startDate));
    //   const mEndDate = moment(this.convertFromString(endDate));
    //   diff = mEndDate.diff(mStartDate, 'days');
    // } else {
      const mStartDate = moment(startDate);
      const mEndDate = moment(endDate);
      diff = mEndDate.diff(mStartDate, 'days');
    // }
    return diff;
  }

  /**
   * Checks if first date is before the second (excludes time)
   * @param firstDate
   * @param secondDate
   */
  isDateBefore(firstDate: Date, secondDate: Date): boolean {
    const mFirstDate = moment(firstDate).hours(0).minutes(0).seconds(0);
    const mSecondDate = moment(secondDate).hours(0).minutes(0).seconds(0);
    return mFirstDate.isBefore(mSecondDate);
  }

  /**
   * Check if the first date is the before or the same as the second date
   * @param firstDate
   * @param secondDate
   */
  isDateSameOrBefore(firstDate: Date, secondDate: Date): boolean {
    const mFirstDate = moment(firstDate).hours(0).minutes(0).seconds(0);
    const mSecondDate = moment(secondDate).hours(0).minutes(0).seconds(0);
    return mFirstDate.isSameOrBefore(mSecondDate, 'days');
  }

  /**
   * Checks if given date is in range between two dates
   * @param dateToTest
   * @param rangeStart
   * @param rangeEnd
   */
  isDateInRange(dateToTest: Date, rangeStart: Date, rangeEnd: Date): boolean {
    const inRange: boolean = moment(dateToTest).isBetween(rangeStart, rangeEnd, 'day', '[]');
    // console.log(`isDateInRange ${dateToTest} in range (${rangeStart}, ${rangeEnd}) = ${inRange}`);
    return inRange;
  }

  /**
   * Checks if the first timestamp is before the second
   * @param firstTimestamp
   * @param secondTimestamp
   */
  isTimestampBefore(firstTimestamp: Date, secondTimestamp: Date): boolean {
    const mFirstTimestamp = moment(firstTimestamp);
    const mSecondTimestamp = moment(secondTimestamp);
    return mFirstTimestamp.isBefore(mSecondTimestamp);
  }

  /**
   * Calculates time in the future expiresInSeconds from now
   * @param expiresInSeconds time of expiration in seconds
   */
  getExpiresAt(expiresInSeconds: number): Date {
    const expiresAt = moment().add(expiresInSeconds, 'seconds');
    return expiresAt.toDate();
  }

  getMaxAgeRestrictionDate(tournamentStartDate: Date): Date {
    const maxDate = moment(tournamentStartDate).subtract(5, 'years');
    return maxDate.toDate();
  }

  getMinAgeRestrictionDate(tournamentStartDate: Date): Date {
    const minDate = moment(tournamentStartDate).subtract(100, 'years');
    return minDate.toDate();
  }

  getAgeOnDate(dateOfBirth: Date, otherDate: Date): number {
    const mDateOfBirth = moment(dateOfBirth);
    const mOtherDate = moment(otherDate);
    return mOtherDate.diff(mDateOfBirth, 'years');
  }

  /**
   * Gets a list of possible starting times for events
   */
  getEventStartingTimes(): any [] {
    const startTimes: any [] = [];
    const pipe: StartTimePipe = new StartTimePipe();
    for (let i = 8; i <= 21; i++) {
      const fullHour = pipe.transform(i);
      startTimes.push({startTime: i, startTimeText: fullHour});
      const halfPastHour = pipe.transform(i + 0.5);
      startTimes.push({startTime: (i + 0.5), startTimeText: halfPastHour});
    }
    return startTimes;
  }

  /**
   * Gets difference between two times in hours and minutes 0:45 or 1:30.
   * @param startTime
   * @param endTime
   */
  getTimeDifferenceAsString(startTime: Date, endTime: Date): string {
    const mStartTime = moment(startTime);
    const mEndTime = moment(endTime);
    const durationMinutes = mEndTime.diff(mStartTime, 'minutes');
    const hours = Math.floor(durationMinutes / 60);
    const minutes = durationMinutes - (hours * 60);
    return (hours > 0) ? `${hours} h ${minutes} m` : `${minutes} m`;
  }

  /**
   * Gets difference between two times in minutes
   * @param startTime
   * @param endTime
   */
  getTimeDifference(startTime: Date, endTime: Date): number {
    const mStartTime = moment(startTime);
    const mEndTime = moment(endTime);
    return mEndTime.diff(mStartTime, 'minutes');
  }
}
