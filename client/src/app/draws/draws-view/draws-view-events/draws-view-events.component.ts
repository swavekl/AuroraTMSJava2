import {Component, Input, OnChanges, SimpleChange, SimpleChanges} from '@angular/core';
import {TournamentEvent} from '../../../tournament/tournament-config/tournament-event.model';
import {Router} from '@angular/router';

@Component({
    selector: 'app-draws-view',
    templateUrl: './draws-view-events.component.html',
    styleUrls: ['./draws-view-events.component.scss'],
    standalone: false
})
export class DrawsViewEventsComponent implements OnChanges {

  @Input()
  public tournamentEvents: TournamentEvent[];

  @Input()
  tournamentStartDate: Date;

  constructor(private router: Router) {
    this.tournamentStartDate = new Date();
  }

  ngOnChanges(changes: SimpleChanges): void {
    const tournamentEventChanges: SimpleChange = changes.tournamentEvents;
    if (tournamentEventChanges != null) {
      const tournamentEvents = tournamentEventChanges.currentValue;
      if (tournamentEvents) {
        this.tournamentEvents = tournamentEvents.sort((event1: TournamentEvent, event2: TournamentEvent) => {
          return event1.ordinalNumber < event2.ordinalNumber ? -1 : 1;
        });
      }
    }
  }

  onSelectEvent(tournamentEvent: TournamentEvent) {
    this.router.navigateByUrl(`/ui/drawsview/${tournamentEvent.tournamentFk}/details/${tournamentEvent.id}`);
  }

  back() {
    this.router.navigateByUrl('/ui/home');
  }
}
