import {Component, Input, OnChanges, OnInit, SimpleChanges, ViewChild} from '@angular/core';
import {Observable} from 'rxjs';
import {TournamentEvent} from '../tournament-event.model';
import {TournamentEventConfigService} from '../tournament-event-config.service';
import {ConfirmationPopupComponent} from '../../../shared/confirmation-popup/confirmation-popup.component';
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
                                      (renumber)="onRenumber($event)">
    </app-tournament-event-config-list>
  `,
  styles: []
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
      this.tournamentEventConfigService.loadTournamentEvents(tournamentId)
        .pipe(tap((events: TournamentEvent[]) => {
          const eventsWithEntries: number [] = [];
          for (const event of events) {
            if (event.numEntries > 0) {
              eventsWithEntries.push(event.id);
            }
          }
          this.eventsWithEntries = eventsWithEntries;
      }));
    }
  }

  onDelete(eventId: number) {
    const hasEntries = (this.eventsWithEntries.indexOf(eventId) >= 0);
    const message = (hasEntries)
      ? 'Warning: There are entries into this event.  You must first remove all entries from this event. Press \'OK\' to close'
      : 'Are you sure you want to delete this event.  Press \'OK\' to proceed';
    const config = {
      width: '450px', height: '250px', data: {
        message: message, showOk: !hasEntries
      }
    };
    const dialogRef = this.dialog.open(ConfirmationPopupComponent, config);
    dialogRef.afterClosed().subscribe(result => {
      if (result === 'ok' && !hasEntries) {
        this.tournamentEventConfigService.delete(eventId);
      }
    });
  }

  onRenumber(updatedEvents: TournamentEvent[]) {
    for (const updatedEvent of updatedEvents) {
      this.tournamentEventConfigService.upsert(updatedEvent);
      // .subscribe(next =>
      // console.log('saved ' + next.name + ' ordinal Number ' + next.ordinalNumber)
      // );
    }
  }

  getTotalPrizeMoney(): number {
    return (this.tournamentEventConfigListComponent) ? this.tournamentEventConfigListComponent.getTotalPrizeMoney() : 0;
  }
}
