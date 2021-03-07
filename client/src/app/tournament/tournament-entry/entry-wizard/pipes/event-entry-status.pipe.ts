import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
  name: 'eventEntryStatus'
})
export class EventEntryStatusPipe implements PipeTransform {

  private theMap = {
    'NOT_ENTERED': 'Not Entered',
    'ENTERED': 'Entered',
    'ENTERED_WAITING_LIST': 'On waiting list',
    'PENDING_CONFIRMATION': 'Pending confirmation',
    'PENDING_DELETION': 'Pending delete confirmation',
    'PENDING_WAITING_LIST': 'Pending Waiting list',
    'RESERVED_WAITING_LIST': ''  // special
  };

  transform(status: any, ...args: unknown[]): string {
    return this.theMap[status];
  }
}
