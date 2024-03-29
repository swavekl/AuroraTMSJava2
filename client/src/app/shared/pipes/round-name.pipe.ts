import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'roundName'
})
export class RoundNamePipe implements PipeTransform {

  transform(round: number, groupNum: number, ...args: unknown[]): unknown {
    switch (round) {
      case 0:
        return 'Round Robin';
      case 2:
        return (groupNum === 1) ? 'Finals' : '3rd & 4th Place';
      case 4:
        return 'Semifinals';
      case 8:
        return 'Quarterfinals';
      default:
        return 'Round of ' + round;
    }
  }

}
