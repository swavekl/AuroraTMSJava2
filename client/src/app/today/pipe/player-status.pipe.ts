import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
  name: 'playerStatus'
})
export class PlayerStatusPipe implements PipeTransform {

  private theMap = {
    'WILL_PLAY': 'Will play',
    'WILL_NOT_PLAY': 'Will not play',
    'WILL_PLAY_BUT_IS_LATE': 'Will play but is late. ETA '  // special
  };

  transform(status: any, eta: string, ...args: unknown[]): string {
    let retValue = this.theMap[status];
    if (status === 'WILL_PLAY_BUT_IS_LATE') {
      if (eta) {
        retValue += eta;
      }
    }
    return retValue;
  }
}
