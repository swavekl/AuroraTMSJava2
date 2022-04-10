import {Component, OnInit} from '@angular/core';
import {TournamentEntryInfoService} from '../../tournament/service/tournament-entry-info.service';
import {Observable} from 'rxjs';
import {TournamentEntryInfo} from '../../tournament/model/tournament-entry-info.model';

@Component({
  selector: 'app-tournament-director-dashboard-container',
  template: `
    <app-tournament-director-dashboard [waitingListEntries]="waitingListEntries$ | async">
    </app-tournament-director-dashboard>
  `,
  styles: [
  ]
})
export class TournamentDirectorDashboardContainerComponent implements OnInit {

  public waitingListEntries$: Observable<TournamentEntryInfo[]>;

  constructor(private tournamentEntryInfoService: TournamentEntryInfoService) { }

  ngOnInit(): void {
    this.waitingListEntries$ = this.tournamentEntryInfoService.getWaitingListEntries(153);
  }

}
