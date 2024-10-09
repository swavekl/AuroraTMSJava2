import {Component, Input} from '@angular/core';
import {UmpiredMatchInfo} from '../model/umpired-match-info.model';

@Component({
  selector: 'app-umpire-match-list',
  templateUrl: './umpire-match-list.component.html',
  styleUrl: './umpire-match-list.component.scss'
})
export class UmpireMatchListComponent {

  @Input()
  singleTournament: boolean = true;

  @Input()
  umpireMatchInfos: UmpiredMatchInfo[] = [];

  @Input()
  umpireName: string;

  showTournamentName(index: number): boolean {
    if (!this.singleTournament) {
      if (index === 0) {
        return true;
      } else {
        const tournamentName = this.umpireMatchInfos[index].tournamentName;
        const previousTournamentName = this.umpireMatchInfos[index - 1].tournamentName;
        return tournamentName !== previousTournamentName;
      }
    } else {
      return false;
    }
  }
}
