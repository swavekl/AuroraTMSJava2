import { Pipe, PipeTransform } from '@angular/core';
import moment from 'moment';
import 'twix';
import {Moment} from 'moment';

// Pipe for formatting date range in a compact way e.g.
// one day tournament date as Apr 4, 2021
// multi day tournament date range as Apr 4 - 6, 2021  or
//                                    Mar 31 - Apr 1, 2022
// invoked with array representing range
// {{ [startDate, endDate] | dateRange }}
@Pipe({
  name: 'dateRange'
})
export class DateRangePipe implements PipeTransform {
  readonly DATE_FORMAT = 'MMM D, yyyy';

  transform(dates: Date[], ...args: unknown[]): string {
    if (dates?.length === 2) {
      const startDate: Date = dates[0];
      const endDate: Date = dates[1];
      const mStartDate: Moment = moment(startDate);
      const mEndDate: Moment = moment(endDate);
      // mStartDate.set('hour', 0);
      // mStartDate.set('minute', 0);
      // mStartDate.utc(true);
      // const startdate = mStartDate.toDate();
      // console.log('startdate', startdate);
      // mEndDate.set('hour', 0);
      // mEndDate.set('minute', 0);
      if (mStartDate.isSame(mEndDate)) {
        return mStartDate.format(this.DATE_FORMAT);
      } else {
        // format as range
        const options = {
          hideTime: true,
          explicitAllDay: true,
          implicitYear : false
        };
        console.log('mStartDate', mStartDate);
        console.log('mEndDate  ', mEndDate);

        return moment(startDate).twix(endDate).format(options);
      }
    } else if (dates?.length === 1) {
      const startDate = dates[0];
      return moment(startDate).format(this.DATE_FORMAT);
    } else {
      return '';
    }
  }

}
