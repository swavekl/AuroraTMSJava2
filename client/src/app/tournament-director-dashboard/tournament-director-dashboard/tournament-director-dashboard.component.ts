import {Component, Input} from '@angular/core';
import { map } from 'rxjs/operators';
import { Breakpoints, BreakpointObserver } from '@angular/cdk/layout';
import {TournamentEntryInfo} from '../../tournament/model/tournament-entry-info.model';

@Component({
  selector: 'app-tournament-director-dashboard',
  templateUrl: './tournament-director-dashboard.component.html',
  styleUrls: ['./tournament-director-dashboard.component.scss']
})
export class TournamentDirectorDashboardComponent {

  @Input()
  waitingListEntries: TournamentEntryInfo[] = [];

  /** Based on the screen size, switch from standard to one column per row */
  cards = this.breakpointObserver.observe(Breakpoints.Handset).pipe(
    map(({ matches }) => {
      if (matches) {
        return [
          { title: 'Card 1', cols: 1, rows: 1 },
          { title: 'Clubs', cols: 1, rows: 1 },
          { title: 'Card 3', cols: 1, rows: 1 },
          { title: 'Card 4', cols: 1, rows: 1 }
        ];
      }

      return [
        { title: 'Players', cols: 1, rows: 1 },
        { title: 'Clubs', cols: 1, rows: 1 },
        { title: 'Referees & Umpires', cols: 1, rows: 1 },
        { title: 'Tournaments', cols: 1, rows: 1 }
      ];
    })
  );

  constructor(private breakpointObserver: BreakpointObserver) {}
}
