import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
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
export class TournamentEventConfigListComponent implements OnInit {

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

  columnsToDisplay: string[] = ['num', 'name', 'day', 'startTime', 'numEntries', 'maxEntries', 'actions'];

  @Output() delete = new EventEmitter();
  @Output() renumber = new EventEmitter();

  constructor(private router: Router,
              public dialog: MatDialog) {
  }

  ngOnInit(): void {
  }

  addEvent() {
    // ask user which event he wants to add and we will preset some default values
    const dialogRef = this.dialog.open(SelectEventDialogComponent);
    dialogRef.afterClosed().subscribe(result => {
      if (result !== false) {
        const url = `tournament/${this.tournamentId}/tournamentevent/create`;
        const selectedEventData = {
          ...result,
          ordinalNumber: this.events.length + 1
        };
        this.router.navigate([url], {state: {data: selectedEventData}});
      }
    });
  }

  getEventEditLink(eventId: number) {
    return `/tournament/${this.tournamentId}/tournamentevent/edit/${eventId}`;
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

  getTotalPrizeMoney(): number {
    let totalPrizeMoney = 0;
    if (this.events && this.events.length > 0) {
      this.events.forEach((event: TournamentEvent) => {
        const eventPrizeInfoList = event?.configuration?.prizeInfoList ?? [];
        let eventTotalPrizeMoney = 0;
        eventPrizeInfoList.forEach(prizeInfo => {
          eventTotalPrizeMoney += isNaN(prizeInfo.prizeMoneyAmount) ? 0 : prizeInfo.prizeMoneyAmount;
        });
        // console.log('event ' + event.name + ' total prize money ' + eventTotalPrizeMoney);
        totalPrizeMoney += eventTotalPrizeMoney;
      });
    }
    // console.log('got totalPrizeMoney', totalPrizeMoney);
    return totalPrizeMoney;
  }
}
