import {Component, Input} from '@angular/core';
import {Tournament} from '../../tournament/tournament-config/tournament.model';
import {DateUtils} from '../../shared/date-utils';
import {TodayService} from '../../shared/today.service';

@Component({
    selector: 'app-referee-tournament-list',
    templateUrl: './referee-tournament-list.component.html',
    styleUrl: './referee-tournament-list.component.scss',
    standalone: false
})
export class RefereeTournamentListComponent {

  @Input()
  tournaments!: Tournament[] | null;
  private today: Date;

  constructor(private todayService: TodayService) {
    this.today = this.todayService.todaysDate;
  }

  isManageUmpiresEnabled(tournamentId: number) {
    let enabled = true;
    for (const tournament of this.tournaments) {
      if (tournament.id === tournamentId) {
        enabled = new DateUtils().isDateInRange(this.today, tournament.startDate, tournament.endDate);
        break;
      }
    }
    return enabled;
  }
}
