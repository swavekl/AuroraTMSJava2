import {Pipe, PipeTransform} from '@angular/core';
import {MatchCardPlayabilityStatus} from '../model/match-info.model';

@Pipe({
  name: 'matchCardStatus'
})
export class MatchCardStatusPipe implements PipeTransform {

  transform(status: MatchCardPlayabilityStatus, playabilityDetail: string, ...args: unknown[]): string {
    let strStatus  = '';
    switch (status) {
      case MatchCardPlayabilityStatus.ReadyToPlay:
        strStatus = 'Ready';
        break;
      case MatchCardPlayabilityStatus.WaitingForPlayer:
        strStatus = playabilityDetail;
        break;
      case MatchCardPlayabilityStatus.WaitingForWinner:
        strStatus = playabilityDetail;
        break;
      case MatchCardPlayabilityStatus.WaitingForBothWinners:
        strStatus = 'Waiting for both prior round winners';
        break;
      case MatchCardPlayabilityStatus.WaitingForPlayersToAdvance:
        strStatus = 'Waiting for both players to advance from RR';
        break;

    }
    return strStatus;
  }

}
