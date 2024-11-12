import {Pipe, PipeTransform} from '@angular/core';
import moment from 'moment';

@Pipe({
  name: 'eventDay'
})
export class EventDayPipePipe implements PipeTransform {

  transform(eventDay: number, tournamentStartDate: any, ...args: unknown[]): string {
    let strEventDayName = '';
    if (tournamentStartDate != null) {
      const localMoment = moment([tournamentStartDate.getFullYear(), tournamentStartDate.getMonth(), tournamentStartDate.getDate(), 0, 0, 0]).utc();
      // event days are 1 based - 1st day will be 1, 2nd day 2, etc.
      localMoment.add(eventDay - 1, 'day');
      strEventDayName = localMoment.format('dddd');  // format date as day of the week
    }
    return strEventDayName;
  }

}
