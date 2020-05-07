import {Component, OnInit, ChangeDetectionStrategy, Input, Output, EventEmitter} from '@angular/core';
import {TournamentEvent} from '../tournament-event.model';

@Component({
  selector: 'app-tournament-event-config',
  templateUrl: './tournament-event-config.component.html',
  styleUrls: ['./tournament-event-config.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TournamentEventConfigComponent implements OnInit {
  @Input()
  tournamentEvent: TournamentEvent;

  @Output()
  saved: EventEmitter<TournamentEvent> = new EventEmitter<TournamentEvent>();

  @Output()
  canceled: EventEmitter<String> = new EventEmitter<String>();

  // days
  days: any [] = [
    {day: 1, dayText: 'Friday'},
    {day: 2, dayText: 'Saturday'},
    {day: 3, dayText: 'Sunday'}
  ];

  startTimes: any [];

  constructor() {
  }

  ngOnInit(): void {
    this.startTimes = [];
    for (let i = 8; i <= 21; i++) {
      const hour = (i < 12) ? i : ((i === 12) ? 12 : (i - 12));
      const AM_PM = (i < 12) ? 'AM' : 'PM';
      const startTimeText = `${hour}:00 ${AM_PM}`;
      this.startTimes.push({startTime: i, startTimeText: startTimeText});
      const startTimeText30 = `${hour}:30 ${AM_PM}`;
      this.startTimes.push({startTime: (i + 0.5), startTimeText: startTimeText30});
    }
  }

  onCancel() {
    this.canceled.emit('cancel');
  }

  onSave(formValues: TournamentEvent) {
    // convert form values to event object
    const tournamentEvent: TournamentEvent = TournamentEvent.toTournamentEvent(formValues);
    this.saved.emit(tournamentEvent);
  }
}
