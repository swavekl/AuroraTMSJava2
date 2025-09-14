import {Component, Input, OnChanges, OnInit, SimpleChanges, ViewChild} from '@angular/core';
import {Observable} from 'rxjs';
import {TournamentEvent} from '../tournament-event.model';
import {TournamentEventConfigService} from '../tournament-event-config.service';
import {MatDialog} from '@angular/material/dialog';
import {TournamentEventConfigListComponent} from './tournament-event-config-list.component';
import {tap} from 'rxjs/operators';

@Component({
    selector: 'app-tournament-event-config-list-container',
    template: `
    <app-tournament-event-config-list [events]="events$ | async"
                                      [startDate]="startDate"
                                      [endDate]="endDate"
                                      [tournamentId]="tournamentId"
                                      [numEventEntries]="numEventEntries"
                                      [maxNumEvenEntries]="maxNumEvenEntries"
                                      (delete)="onDelete($event)"
                                      (update)="onUpdateMultiple($event)">
    </app-tournament-event-config-list>
  `,
    styles: [],
    standalone: false
})
export class TournamentEventConfigListContainerComponent implements OnInit, OnChanges {

  // id of tournament whose events we are showing
  @Input()
  tournamentId: number;

  @Input()
  numEventEntries: number;

  @Input()
  maxNumEvenEntries: number;

  @Input()
  startDate: Date;

  @Input()
  endDate: Date;

  events$: Observable<TournamentEvent []>;

  private eventsWithEntries: number [] = [];

  @ViewChild(TournamentEventConfigListComponent)
  tournamentEventConfigListComponent: TournamentEventConfigListComponent;

  constructor(private tournamentEventConfigService: TournamentEventConfigService,
              private dialog: MatDialog) {
    this.events$ = this.tournamentEventConfigService.entities$;
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.tournamentId != null) {
      const tournamentId = changes.tournamentId.currentValue;
      if (tournamentId != null) {
        // subscribed by the template
        this.tournamentEventConfigService.loadTournamentEvents(tournamentId)
          .pipe(tap((events: TournamentEvent[]) => {
          }));
      }
    }
  }

  onDelete(eventId: number) {
    this.tournamentEventConfigService.delete(eventId);
  }

  onUpdateMultiple(updatedEvents: TournamentEvent[]) {
    for (const updatedEvent of updatedEvents) {
      this.tournamentEventConfigService.upsert(updatedEvent);
    }
  }

  getTotalPrizeMoney(): number {
    return (this.tournamentEventConfigListComponent) ? this.tournamentEventConfigListComponent.getTotalPrizeMoney() : 0;
  }

  getMaxNumEventEntries(): number {
    return (this.tournamentEventConfigListComponent) ? this.tournamentEventConfigListComponent.getMaxNumEventEntries() : 0;
  }
}
