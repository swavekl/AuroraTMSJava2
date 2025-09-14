import {Pipe, PipeTransform} from '@angular/core';
import moment from 'moment';

@Pipe({
    name: 'eventDay',
    standalone: false
})
export class EventDayPipePipe implements PipeTransform {

  transform(eventDay: number, tournamentStartDate: any, ...args: unknown[]): string {
    let strEventDayName = '';
    if (tournamentStartDate != null) {
      const localMoment = moment(tournamentStartDate).utc();
      localMoment.add(eventDay - 1, 'day');
      // event days are 1 based - 1st day will be 1, 2nd day 2, etc.
      strEventDayName = localMoment.format('dddd');  // format date as day of the week
    }
    return strEventDayName;
  }

}
