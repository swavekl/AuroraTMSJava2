import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
  name: 'availabilityStatus'
})
export class AvailabilityStatusPipe implements PipeTransform {
  private theMap = {
    'ALREADY_ENTERED': 'Entered',
    'AVAILABLE_FOR_ENTRY': 'Available',
    'EVENT_FULL': 'Waiting list',
    'DISQUALIFIED_BY_AGE': 'Disqualified by age',
    'DISQUALIFIED_BY_GENDER': 'Disqualified by gener',
    'DISQUALIFIED_BY_RATING': 'Disqualified by rating',
    'SCHEDULING_CONFLICT': 'Scheduling conflict',
    'MAX_EVENTS_PER_DAY': 'Maximum events per day reached',
    'MAX_EVENTS_PER_TOURNAMENT': 'Maximum events per tournament reached'
  };

  transform(status: any, ...args: unknown[]): string {
    return this.theMap[status];
  }
}
