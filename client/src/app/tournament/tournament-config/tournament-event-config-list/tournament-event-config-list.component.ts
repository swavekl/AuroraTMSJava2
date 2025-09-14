import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChange, SimpleChanges} from '@angular/core';
import {TournamentEvent} from '../tournament-event.model';
import {Router} from '@angular/router';
import {MatDialog} from '@angular/material/dialog';
import {SelectEventDialogComponent} from '../select-event-dialog/select-event-dialog.component';
import {DateUtils} from '../../../shared/date-utils';
import {CdkDragDrop, moveItemInArray} from '@angular/cdk/drag-drop';
import {ConfirmationPopupComponent} from '../../../shared/confirmation-popup/confirmation-popup.component';
import {AddManyEventsDialogComponent} from '../add-many-events-dialog/add-many-events-dialog.component';
import {Subscription} from 'rxjs';
import {Tournament} from '../tournament.model';

@Component({
    selector: 'app-tournament-event-config-list',
    templateUrl: './tournament-event-config-list.component.html',
    styleUrls: ['./tournament-event-config-list.component.scss'],
    standalone: false
})
export class TournamentEventConfigListComponent implements OnInit, OnChanges, OnDestroy {

  // tournament events
  @Input()
  events: TournamentEvent [];

  @Input()
  numEventEntries: number;

  @Input()
  maxNumEvenEntries: number;

  // tournament start and end dates
  @Input()
  startDate: Date;

  @Input()
  endDate: Date;

  @Input()
  tournamentId: number;

  columnsToDisplay: string[] = ['num', 'name', 'day', 'startTime', 'fee', 'numEntries', 'actions'];

  @Output() delete = new EventEmitter();
  @Output() update = new EventEmitter();

  private subscriptions: Subscription = new Subscription();

  constructor(private router: Router,
              public dialog: MatDialog) {
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  ngOnChanges(changes: SimpleChanges): void {
    const eventChanges: SimpleChange = changes.events;
    if (eventChanges) {
      const events = eventChanges.currentValue;
      if (events && events.length > 0) {
        events.sort((left: TournamentEvent, right: TournamentEvent) => {
          return (left.ordinalNumber === right.ordinalNumber ? 0 : (
            left.ordinalNumber > right.ordinalNumber ? 1 : -1
          ));
        });
      }
    }
  }

  addEvent() {
    // ask user which event he wants to add and we will preset some default values
    const dialogRef = this.dialog.open(SelectEventDialogComponent);
    dialogRef.afterClosed().subscribe(result => {
      if (result !== false) {
        const url = `/ui/tournamentsconfig/tournament/${this.tournamentId}/tournamentevent/create`;
        const selectedEventData = {
          ...result,
          ordinalNumber: this.events.length + 1
        };
        this.router.navigate([url], {state: {data: selectedEventData}});
      }
    });
  }

  getEventEditLink(eventId: number) {
    return `/ui/tournamentsconfig/tournament/${this.tournamentId}/tournamentevent/edit/${eventId}`;
  }

  deleteEvent(eventId: number) {
    console.log('delete event ', eventId);
    // check if has events
    let hasEntries = false;
    for (const event of this.events) {
      if (event.id === eventId) {
        hasEntries = event.numEntries > 0;
        break;
      }
    }

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
        this.delete.emit(eventId);
      }
    });
  }

  getDayOfWeek(day: number) {
    if (this.startDate != null) {
      return new DateUtils().getDayAsString(this.startDate, day);
    } else {
      return '';
    }
  }

  getStartTime(startTime: number) {
    if (this.startDate != null) {
      return new DateUtils().getTimeAsString(this.startDate, startTime);
    } else {
      return '';
    }
  }

  renumberEvents() {
    const renumberedEvents: TournamentEvent [] = [];
    for (let i = 0; i < this.events.length; i++) {
      const event = this.events[i];
      renumberedEvents.push({
        ...event,
        ordinalNumber: i + 1
      });
    }
    this.update.emit(renumberedEvents);
  }

  /**
   * Calculates total prize money for all events in this tournament
   */
  getTotalPrizeMoney(): number {
    let totalPrizeMoney = 0;
    if (this.events && this.events.length > 0) {
      this.events.forEach((event: TournamentEvent) => {
        const eventPrizeInfoList = event?.configuration?.prizeInfoList ?? [];
        let eventTotalPrizeMoney = 0;
        eventPrizeInfoList.forEach(prizeInfo => {
          // for a range of places we need to multiply by number of places
          const numAwardedPlaces = (isNaN(prizeInfo.awardedForPlaceRangeEnd) || (prizeInfo.awardedForPlaceRangeEnd === 0)) ?
            1 : (prizeInfo.awardedForPlaceRangeEnd - prizeInfo.awardedForPlace + 1);
          const prizeMoneyAmount = isNaN(prizeInfo.prizeMoneyAmount) ? 0 : prizeInfo.prizeMoneyAmount;
          eventTotalPrizeMoney += numAwardedPlaces * prizeMoneyAmount;
        });
        totalPrizeMoney += eventTotalPrizeMoney;
      });
    }
    return totalPrizeMoney;
  }

  getMaxNumEventEntries(): number {
    let maxNumEventEntries = 0;
    if (this.events && this.events.length > 0) {
      this.events.forEach((event: TournamentEvent) => {
        maxNumEventEntries += event.maxEntries;
      });
    }
    return maxNumEventEntries;
  }

  drop(dndEvent: CdkDragDrop<string[]>) {
    moveItemInArray(this.events, dndEvent.previousIndex, dndEvent.currentIndex);
    const renumberedEvents: TournamentEvent [] = [];
    const updatedEvents: TournamentEvent [] = [];
    for (let i = 0; i < this.events.length; i++) {
      const event: TournamentEvent = this.events[i];
      const updatedEvent: TournamentEvent = {
        ...event,
        ordinalNumber: i + 1
      };
      renumberedEvents.push(updatedEvent);
      if (event.ordinalNumber != updatedEvent.ordinalNumber) {
        updatedEvents.push(updatedEvent);
      }
    }

    this.events = renumberedEvents;
    this.update.emit(updatedEvents);
  }

  addManyEvent() {
      const config = {
        width: '750px', height: '580px', data: {tournamentId: this.tournamentId}

      };
      const dialogRef = this.dialog.open(AddManyEventsDialogComponent, config);
      const subscription = dialogRef.afterClosed().subscribe(result => {
        if (result.action === 'ok') {
          let ordinalNumber = 1;
          this.events.forEach((existingEvent: TournamentEvent) => {
            ordinalNumber = Math.max(ordinalNumber, existingEvent.ordinalNumber);
          });

          // merge old and new events
          let mergedEvents: TournamentEvent[] = [...this.events];
          result.configuredEvents.forEach((newEvent: TournamentEvent) => {
            let foundIndex = -1;
            const foundEvent: TournamentEvent = mergedEvents.find((existingEvent: TournamentEvent, index: number) => {
              const found: boolean = (existingEvent.name === newEvent.name);
              if (found) {
                foundIndex = index;
              }
              return found;
            });
            if (foundIndex === -1) {
              // add new one at the end
              mergedEvents.push(newEvent);
            } else {
              // replace existing event
              mergedEvents.splice(foundIndex, 1, newEvent);
            }
          });

          // reorder them
          let orderedEvents: TournamentEvent[] = [];
          mergedEvents.forEach((tournamentEvent: TournamentEvent, index: number) => {
            orderedEvents.push({...tournamentEvent, ordinalNumber: index + 1});
          });

          this.events = orderedEvents;

          this.update.emit(this.events);
      }
    });
    this.subscriptions.add(subscription);

  }
}
