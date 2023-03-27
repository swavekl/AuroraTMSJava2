import {Component, Input} from '@angular/core';
import {TournamentEvent} from '../../../tournament/tournament-config/tournament-event.model';
import {Router} from '@angular/router';

@Component({
  selector: 'app-draws-view',
  templateUrl: './draws-view-events.component.html',
  styleUrls: ['./draws-view-events.component.scss']
})
export class DrawsViewEventsComponent {

  @Input()
  public tournamentEvents: TournamentEvent[];

  @Input()
  tournamentStartDate: Date;

  constructor(private router: Router) {
    this.tournamentStartDate = new Date();
  }

  onSelectEvent(tournamentEvent: TournamentEvent) {
    this.router.navigateByUrl(`/ui/drawsview/${tournamentEvent.tournamentFk}/details/${tournamentEvent.id}`);
  }
}
