import {Pipe, PipeTransform} from '@angular/core';
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
    name: 'dateRange',
    standalone: false
})
export class DateRangePipe implements PipeTransform {
  readonly DATE_FORMAT = 'MMM D, yyyy';
  readonly TIME_PORTION: string = 'T00:00:00.000+00:00';

  transform(dates: any[], ...args: unknown[]): string {
    if (dates?.length === 2) {
      let startDate: any = dates[0];
      let endDate: any = dates[1];
      if (typeof startDate === 'string') {
        startDate = startDate.substring(0, startDate.indexOf('T'));
        startDate += this.TIME_PORTION;
      }
      if (typeof endDate === 'string') {
        endDate = endDate.substring(0, endDate.indexOf('T'));
        endDate += this.TIME_PORTION;
      }
      const mStartDate: Moment = moment(startDate).utc();
      const mEndDate: Moment = moment(endDate).utc();
      if (mStartDate.isSame(mEndDate)) {
        return mStartDate.format(this.DATE_FORMAT);
      } else {
        // format as range
        const options = {
          hideTime: true,
          explicitAllDay: true,
          implicitYear: false
        };
        return mStartDate.twix(mEndDate).format(options);
      }
    } else if (dates?.length === 1) {
      const startDate = dates[0];
      return moment(startDate).format(this.DATE_FORMAT);
    } else {
      return '';
    }
  }

}
