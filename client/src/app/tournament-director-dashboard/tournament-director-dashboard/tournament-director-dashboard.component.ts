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
          { title: 'Confirmed Players', cols: 1, rows: 1, outletName: 'ploutlet' },
          { title: 'Payments & Refunds', cols: 1, rows: 1, outletName: 'proutlet' },
          { title: 'Waiting List', cols: 1, rows: 1, outletName: 'wloutlet' }
        ];
      }

      return [
        { title: 'Payments & Refunds', cols: 1, rows: 2, outletName: 'proutlet' },
        { title: 'Confirmed Players', cols: 1, rows: 1, outletName: 'ploutlet' },
        { title: 'Waiting List', cols: 1, rows: 1, outletName: 'wloutlet' }
      ];
    })
  );

  constructor(private breakpointObserver: BreakpointObserver) {}
}
