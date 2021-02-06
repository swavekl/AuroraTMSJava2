import {Pipe, PipeTransform} from '@angular/core';
import * as moment from 'moment';

@Pipe({
  name: 'eventDay'
})
export class EventDayPipePipe implements PipeTransform {

  transform(eventDay: number, eventStartDate: Date, ...args: unknown[]): string {
    let strEventDayName = '';
    if (eventStartDate != null) {
      const localMoment = moment([eventStartDate.getFullYear(), eventStartDate.getMonth(), eventStartDate.getDate(), 0, 0, 0]).local();
      // event days are 1 based - 1st day will be 1, 2nd day 2, etc.
      localMoment.add(eventDay - 1, 'day');
      strEventDayName = localMoment.format('dddd');  // format date as day of the week
    }
    return strEventDayName;
  }

}
