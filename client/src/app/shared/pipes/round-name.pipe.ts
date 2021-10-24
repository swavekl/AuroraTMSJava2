import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'roundName'
})
export class RoundNamePipe implements PipeTransform {

  transform(round: number, ...args: unknown[]): unknown {
    switch (round) {
      case 0:
        return 'Round Robin';
      case 2:
        return 'Finals';
      case 4:
        return 'Semi-Finals';
      case 8:
        return 'Quarter-Finals';
      default:
        return 'Round of ' + round;
    }
  }

}
