import {Pipe, PipeTransform} from '@angular/core';
import {MatchCardPlayabilityStatus} from '../model/match-info.model';

@Pipe({
  name: 'matchCardStatus'
})
export class MatchCardStatusPipe implements PipeTransform {

  transform(status: MatchCardPlayabilityStatus, ...args: unknown[]): unknown {
    let strStatus  = '';
    switch (status) {
      case MatchCardPlayabilityStatus.ReadyToPlay:
        strStatus = 'Ready';
        break;
      case MatchCardPlayabilityStatus.WaitingForPlayer:
        strStatus = 'Waiting for player';
        break;
      case MatchCardPlayabilityStatus.WaitingForWinner:
        strStatus = 'Waiting for winner';
        break;
      case MatchCardPlayabilityStatus.WaitingForBothWinners:
        strStatus = 'Waiting for both prior round winners';
        break;
      case MatchCardPlayabilityStatus.WaitingForPlayersToAdvance:
        strStatus = 'Waiting for players to advance';
        break;

    }
    return strStatus;
  }

}
