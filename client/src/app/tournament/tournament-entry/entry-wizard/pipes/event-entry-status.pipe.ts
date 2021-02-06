import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
  name: 'eventEntryStatus'
})
export class EventEntryStatusPipe implements PipeTransform {

  private theMap = {
    'NOT_ENTERED': 'Not Entered',
    'CONFIRMED': 'Confirmed',
    'PENDING_CONFIRMATION': 'Pending confirmation',
    'PENDING_DELETION': 'Pending delete confirmation',
    'WAITING_LIST': 'Waiting list',
    'ENTERED_WAITING_LIST': 'On waiting list',
    'DISQUALIFIED_RATING': 'Disqualified by rating',
    'DISQUALIFIED_AGE': 'Disqualified by age',
    'DISQUALIFIED_GENDER': 'Disqualified by gender',
    'DISQUALIFIED_TIME_CONFLICT': 'Time conflict'
  };

  transform(status: any, ...args: unknown[]): string {
    return this.theMap[status];
  }
}
