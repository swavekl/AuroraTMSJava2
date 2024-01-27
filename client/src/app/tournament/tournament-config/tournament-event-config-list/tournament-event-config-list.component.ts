import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChange, SimpleChanges} from '@angular/core';
import {TournamentEvent} from '../tournament-event.model';
import {Router} from '@angular/router';
import {MatDialog} from '@angular/material/dialog';
import {SelectEventDialogComponent} from '../select-event-dialog/select-event-dialog.component';
import {DateUtils} from '../../../shared/date-utils';

@Component({
  selector: 'app-tournament-event-config-list',
  templateUrl: './tournament-event-config-list.component.html',
  styleUrls: ['./tournament-event-config-list.component.css']
})
export class TournamentEventConfigListComponent implements OnInit, OnChanges {

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
  @Output() renumber = new EventEmitter();

  constructor(private router: Router,
              public dialog: MatDialog) {
  }

  ngOnInit(): void {
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

  editEvent(eventId: number) {
    const url = this.getEventEditLink(eventId);
    this.router.navigate([url]);
  }

  deleteEvent(eventId: number) {
    this.delete.emit(eventId);
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
    this.renumber.emit(renumberedEvents);
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
}
