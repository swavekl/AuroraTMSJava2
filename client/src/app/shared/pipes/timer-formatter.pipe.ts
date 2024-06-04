import { Pipe, PipeTransform } from '@angular/core';

/**
 * Formats timeout values as 1:34 digital display
 */
@Pipe({
  name: 'timerFormatter',
  standalone: true
})
export class TimerFormatterPipe implements PipeTransform {

  /**
   * @param timerValue in seconds
   * @param args
   */
  transform(timerValue: number, ...args: unknown[]): string {
    const minutes: number = Math.floor(timerValue / 60);
    const seconds: number = timerValue % 60;
    return (seconds >= 10) ? `${minutes}:${seconds}` : `${minutes}:0${seconds}`;
  }

}
