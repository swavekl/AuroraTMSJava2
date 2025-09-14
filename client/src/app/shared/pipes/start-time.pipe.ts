import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'startTime',
    standalone: false
})
export class StartTimePipe implements PipeTransform {

  transform(value: number, ...args: unknown[]): string {
    const AM_PM = (value < 12) ? 'AM' : 'PM';
    const isFullHour = (value === Math.floor(value));
    let hour: number = Math.floor(value);
    hour = (hour === 12) ? hour : ((hour < 12) ? hour : (hour - 12));
    return (isFullHour) ? `${hour}:00 ${AM_PM}` : `${hour}:30 ${AM_PM}`;
  }

}
