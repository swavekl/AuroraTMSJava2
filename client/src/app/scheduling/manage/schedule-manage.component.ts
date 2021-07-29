import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChange, SimpleChanges} from '@angular/core';
import {DateUtils} from '../../shared/date-utils';
import {Tournament} from '../../tournament/tournament-config/tournament.model';
import {EventDayPipePipe} from '../../shared/pipes/event-day-pipe.pipe';
import {createSelector} from '@ngrx/store';
import {MatchCard} from '../../matches/model/match-card.model';

@Component({
  selector: 'app-schedule-manage',
  templateUrl: './schedule-manage.component.html',
  styleUrls: ['./schedule-manage.component.scss']
})
export class ScheduleManageComponent implements OnInit, OnChanges {

  @Input()
  public tournament: Tournament;

  @Input()
  public matchCards: MatchCard [] = [];

  @Output()
  public dayChangedEvent: EventEmitter<number> = new EventEmitter<number>();

  @Output()
  public generateScheduleForEvent: EventEmitter<number> = new EventEmitter<number>();

  // days
  days: any [] = [];

  // selected tournament day for which to show the events
  selectedDay: number;

  // array to be used for *ngFor interation
  startingTimes: any [] = [];

  // array to be used for *ngFor iteration
  tablesArray: any [] = [];

  constructor() {
    this.startingTimes = new DateUtils().getEventStartingTimes();
    this.selectedDay = 1;
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    const tournamentChanges: SimpleChange = changes.tournament;
    if (tournamentChanges != null) {
      const tournament = tournamentChanges.currentValue;
      if (tournament != null) {
        const numTables = tournament.configuration.numberOfTables || 1;
        this.tablesArray = Array(numTables);
        this.buildTournamentDaysOptions(tournament);
      }
    }
  }

  private buildTournamentDaysOptions (tournament: Tournament) {
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
    this.selectedDay = 1;
  }

  onDayChange($event: any) {
    this.selectedDay = $event.value;
    console.log ('day', this.selectedDay);
    this.dayChangedEvent.emit(this.selectedDay);
  }

  onGenerateSchedule() {
    this.generateScheduleForEvent.emit(this.selectedDay);
  }

}
