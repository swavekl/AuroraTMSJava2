import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
    name: 'eventEntryStatus',
    standalone: false
})
export class EventEntryStatusPipe implements PipeTransform {

  private theMap = {
    'NOT_ENTERED': 'Not Entered',
    'ENTERED': 'Confirmed',
    'ENTERED_WAITING_LIST': 'On waiting list',
    'PENDING_CONFIRMATION': 'Pending',
    'PENDING_DELETION':     'Pending',
    'PENDING_WAITING_LIST': 'Pending',
    'RESERVED_WAITING_LIST': ''  // special
  };

  transform(status: any, ...args: unknown[]): string {
    return this.theMap[status];
  }
}
