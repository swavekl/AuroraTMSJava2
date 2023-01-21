import {Component, Input} from '@angular/core';
import {map} from 'rxjs/operators';
import {BreakpointObserver, Breakpoints} from '@angular/cdk/layout';
import {Router} from '@angular/router';

@Component({
  selector: 'app-tournament-director-dashboard',
  templateUrl: './tournament-director-dashboard.component.html',
  styleUrls: ['./tournament-director-dashboard.component.scss']
})
export class TournamentDirectorDashboardComponent {

  @Input()
  tournamentId: number;

  @Input()
  tournamentName: string;

  /** Based on the screen size, switch from standard to one column per row */
  cards = this.breakpointObserver.observe(Breakpoints.Handset).pipe(
    map(({matches}) => {
      if (matches) {
        return [
          {title: 'Confirmed Players', cols: 1, rows: 1, outletName: 'ploutlet'},
          {title: 'Payments & Refunds', cols: 1, rows: 1, outletName: 'proutlet'},
          {title: 'Waiting List', cols: 1, rows: 1, outletName: 'wloutlet'}
        ];
      }

      return [
        {title: 'Payments & Refunds', cols: 1, rows: 2, outletName: 'proutlet'},
        {title: 'Confirmed Players', cols: 1, rows: 1, outletName: 'ploutlet'},
        {title: 'Waiting List', cols: 1, rows: 1, outletName: 'wloutlet'}
      ];
    })
  );

  constructor(private breakpointObserver: BreakpointObserver,
              private router: Router) {
  }

  onRegisterPlayer() {
    this.router.navigate(['/tdadduserprofile', this.tournamentId]);
  }
}
