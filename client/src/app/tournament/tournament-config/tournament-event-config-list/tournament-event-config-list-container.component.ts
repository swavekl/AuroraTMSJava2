import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {Observable} from 'rxjs';
import {TournamentEvent} from '../tournament-event.model';
import {TournamentEventConfigService} from '../tournament-event-config.service';
import {ConfirmationPopupComponent, ConfirmationPopupData} from '../../../shared/confirmation-popup/confirmation-popup.component';
import {MatDialog} from '@angular/material/dialog';

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

  constructor(private tournamentEventConfigService: TournamentEventConfigService,
              private dialog: MatDialog) {
    this.events$ = this.tournamentEventConfigService.entities$;
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.tournamentId != null) {
      const tournamentId = changes.tournamentId.currentValue;
      this.tournamentEventConfigService.loadTournamentEvents(tournamentId);
    }
  }

  onDelete(eventId: number) {
    const config = {
      width: '450px', height: '250px', data: {
        message: 'There are entries into this event.  Are you sure you want to delete this event.  Press \'OK\' to proceed',
      }
    };
    const dialogRef = this.dialog.open(ConfirmationPopupComponent, config);
    dialogRef.afterClosed().subscribe(result => {
      if (result === 'ok') {
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
}
