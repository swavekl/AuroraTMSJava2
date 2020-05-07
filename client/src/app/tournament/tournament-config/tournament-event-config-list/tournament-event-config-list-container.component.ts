import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {Observable} from 'rxjs';
import {TournamentEvent} from '../tournament-event.model';
import {TournamentEventConfigService} from '../tournament-event-config.service';

@Component({
  selector: 'app-tournament-event-config-list-container',
  template: `
    <app-tournament-event-config-list [events]="events$ | async"
                                      [startDate]="startDate"
                                      [tournamentId]="tournamentId"
    (delete)="onDelete($event)">
    </app-tournament-event-config-list>
  `,
  styles: []
})
export class TournamentEventConfigListContainerComponent implements OnInit, OnChanges {

  // id of tournament whose events we are showing
  @Input()
  tournamentId: number;

  @Input()
  startDate: Date;

  events$: Observable<TournamentEvent []>;

  constructor(private tournamentEventConfigService: TournamentEventConfigService) {
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.tournamentId.currentValue != null) {
      this.events$ = this.tournamentEventConfigService.getAllForTournament(changes.tournamentId.currentValue);
      this.events$.subscribe(data => {
        console.log('got tournament events data ' + JSON.stringify(data));
        return data;
      });
    }
  }

  onDelete(eventId: number) {
    this.tournamentEventConfigService.delete(eventId);
    // todo refresh the list
  }
}
