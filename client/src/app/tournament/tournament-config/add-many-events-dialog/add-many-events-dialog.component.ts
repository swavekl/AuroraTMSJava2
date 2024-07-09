import {Component, Inject, OnDestroy} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {STEPPER_GLOBAL_OPTIONS} from '@angular/cdk/stepper';
import {EventDefaults} from '../model/event-defaults';
import {createSelector} from '@ngrx/store';
import {Observable, Subscription} from 'rxjs';
import {Tournament} from '../tournament.model';
import {DateUtils} from '../../../shared/date-utils';
import {EventDayPipePipe} from '../../../shared/pipes/event-day-pipe.pipe';
import {TournamentConfigService} from '../tournament-config.service';
import {TournamentEvent} from '../tournament-event.model';
import {TournamentEventConfiguration} from '../model/tournament-event-configuration.model';
import {PrizeInfo} from '../model/prize-info.model';
import {CdkDragDrop, moveItemInArray} from '@angular/cdk/drag-drop';
import {DrawMethod} from '../model/draw-method.enum';

@Component({
  selector: 'app-add-many-events-dialog',
  templateUrl: './add-many-events-dialog.component.html',
  styleUrl: './add-many-events-dialog.component.scss',
  providers: [
    {
      provide: STEPPER_GLOBAL_OPTIONS,
      useValue: {showError: true}
    }
  ]
})
export class AddManyEventsDialogComponent implements OnDestroy {
  tournamentId: number;
  public availableEvents: any[];
  public selectedEvents: TournamentEvent[];

  days: any [] = [];
  startTimes: any [];

  private subscriptions: Subscription = new Subscription();
  abc: any;

  columnsToDisplay: string[] = ['num', 'name'];

  private addingEvent: boolean;


  constructor(private tournamentConfigService: TournamentConfigService,
              public dialogRef: MatDialogRef<AddManyEventsDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any) {
    this.tournamentId = data?.tournamentId;
    const eventDefaults = new EventDefaults().eventDefaults;
    this.availableEvents = [...eventDefaults];
    this.selectedEvents = [];
    this.initializeDaysChoices(this.tournamentId);
    this.startTimes = new DateUtils().getEventStartingTimes();
    this.addingEvent = true;
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  private initializeDaysChoices(tournamentId: number) {
    const selectedEntrySelector = createSelector(
      this.tournamentConfigService.selectors.selectEntityMap,
      (entityMap) => {
        return entityMap[tournamentId];
      });
    const selectedEntry$: Observable<Tournament> = this.tournamentConfigService.store.select(selectedEntrySelector);
    const subscription = selectedEntry$.subscribe((tournament: Tournament) => {
      if (tournament != null) {
        const startDate = tournament.startDate;
        const endDate = tournament.endDate || startDate;
        const dateUtils = new DateUtils();
        const tournamentDuration = dateUtils.daysBetweenDates(startDate, endDate) + 1;
        const pipe: EventDayPipePipe = new EventDayPipePipe();
        const days: any [] = [];
        const mStartDate = new DateUtils().convertFromString(startDate);
        for (let day = 1; day <= tournamentDuration; day++) {
          const dayText = pipe.transform(day, mStartDate);
          days.push({day: day, dayText: dayText});
        }
        this.days = days;
      }
    });
    this.subscriptions.add(subscription);
  }


  onCancel() {
    this.dialogRef.close({action: 'cancel'});
  }

  onOk() {
    this.dialogRef.close({action: 'ok', configuredEvents: this.selectedEvents});

  }

  onSelectedEvent(selectedEvent: TournamentEvent) {
    if (this.addingEvent) {
      const config = new TournamentEventConfiguration();
      for (let i = 0; i < 3; i++) {
        const prizeInfo = new PrizeInfo();
        prizeInfo.awardedForPlace = i + 1;
        config.prizeInfoList.push(prizeInfo);
      }
      let maxOrdinalNumber = 0;
      for (const selectedEvent of this.selectedEvents) {
        maxOrdinalNumber = Math.max(maxOrdinalNumber, selectedEvent.ordinalNumber);
      }
      const drawMethod: DrawMethod = (selectedEvent.drawMethod != null) ? selectedEvent.drawMethod : DrawMethod.SNAKE;
      const playersPerGroup: number = (selectedEvent.playersPerGroup != null) ? selectedEvent.playersPerGroup : 4;
      let clonedEvent: TournamentEvent = {
        ...selectedEvent,
        tournamentFk: this.tournamentId,
        ordinalNumber: maxOrdinalNumber + 1,
        drawMethod: drawMethod,
        playersToAdvance: 1,
        playersToSeed: 0,
        maxEntries: 32,
        numEntries: 0,
        feeAdult: 30,
        feeJunior: 30,
        playersPerGroup: playersPerGroup,
        numberOfGames: 5,
        numberOfGamesSEPlayoffs: 5,
        numberOfGamesSEQuarterFinals: 5,
        numberOfGamesSESemiFinals: 5,
        numberOfGamesSEFinals: 5,
        advanceUnratedWinner: false,
        pointsPerGame: 11,
        play3rd4thPlace: false,
        configuration: config
      };
      this.selectedEvents = [...this.selectedEvents, clonedEvent];
    } else {
      // delete the event
      this.selectedEvents = this.selectedEvents.filter((tournamentEvent: TournamentEvent, index: number) => {
        return (tournamentEvent.name !== selectedEvent.name);
      });
    }
  }

  drop(dndEvent: CdkDragDrop<string[]>) {
    moveItemInArray(this.selectedEvents, dndEvent.previousIndex, dndEvent.currentIndex);
    const renumberedEvents: TournamentEvent [] = [];
    for (let i = 0; i < this.selectedEvents.length; i++) {
      const event: TournamentEvent = this.selectedEvents[i];
      const updatedEvent: TournamentEvent = {
        ...event,
        ordinalNumber: i + 1
      };
      renumberedEvents.push(updatedEvent);
    }

    this.selectedEvents = renumberedEvents;
  }

  onSelectionChange($event: boolean) {
    this.addingEvent = $event;
  }
}
