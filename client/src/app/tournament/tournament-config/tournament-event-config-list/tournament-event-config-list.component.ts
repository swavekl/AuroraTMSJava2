import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {TournamentEvent} from '../tournament-event.model';
import {Router} from '@angular/router';
import {MatDialog} from '@angular/material/dialog';
import {SelectEventDialogComponent} from '../select-event-dialog/select-event-dialog.component';

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
  startDate: Date;

  @Input()
  tournamentId: number;

  columnsToDisplay: string[] = ['num', 'name', 'day', 'startTime', 'actions'];

  // save and cancel
  @Output() delete = new EventEmitter();

  constructor(private router: Router,
              public dialog: MatDialog) {
  }

  ngOnInit(): void {
  }

  getDayOfWeek(day: number) {
    const eventDay = this.startDate.getDay() + (day - 1);
    const weekday = new Array(7);
    weekday[0] = 'Sunday';
    weekday[1] = 'Monday';
    weekday[2] = 'Tuesday';
    weekday[3] = 'Wednesday';
    weekday[4] = 'Thursday';
    weekday[5] = 'Friday';
    weekday[6] = 'Saturday';
    return weekday[eventDay];
  }

  addEvent() {
    // ask user which event he wants to add and we will preset some default values
    const dialogRef = this.dialog.open(SelectEventDialogComponent);
    dialogRef.afterClosed().subscribe(result => {
      if (result !== false) {
        const url = `tournament/${this.tournamentId}/tournamentevent/create`;
        const selectedEventData = {
          ...result,
          ordinalNumber : this.events.length + 1
        };
        this.router.navigate([url], {state: {data: selectedEventData}});
      }
    });
  }

  editEvent(eventId: number) {
    const url = `tournament/${this.tournamentId}/tournamentevent/edit/${eventId}`;
    this.router.navigate([url]);
  }

  deleteEvent(eventId: number) {
    this.delete.emit(eventId);
  }

  getStartTime(startTime: number) {
    const fractionOfHour = (startTime % 1).toFixed(2);
    const hours = Math.floor(startTime);
    // @ts-ignore
    const minutes = 60 * fractionOfHour;
    const strMinutes = (minutes === 0) ? '00' : minutes.toPrecision(2);
    return hours + ':' + strMinutes;
  }

  renumberEvents() {
    console.log ('reordering events');
  }
}
