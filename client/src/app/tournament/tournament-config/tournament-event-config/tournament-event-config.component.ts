import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChange,
  SimpleChanges
} from '@angular/core';
import {TournamentEvent} from '../tournament-event.model';
import {StartTimePipe} from '../../../shared/pipes/start-time.pipe';
import {EventDayPipePipe} from '../../../shared/pipes/event-day-pipe.pipe';
import {createSelector} from '@ngrx/store';
import {Observable, Subscription} from 'rxjs';
import {DateUtils} from '../../../shared/date-utils';
import {TournamentConfigService} from '../tournament-config.service';
import {Tournament} from '../tournament.model';

@Component({
  selector: 'app-tournament-event-config',
  templateUrl: './tournament-event-config.component.html',
  styleUrls: ['./tournament-event-config.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TournamentEventConfigComponent implements OnInit, OnChanges, OnDestroy {
  @Input()
  tournamentEvent: TournamentEvent;

  @Output()
  saved: EventEmitter<TournamentEvent> = new EventEmitter<TournamentEvent>();

  @Output()
  canceled: EventEmitter<String> = new EventEmitter<String>();

  // days
  days: any [] = [];

  startTimes: any [];

  // gender restrictions
  genderRestrictions: any [] =  [
    {value: 'NONE', label: 'None'},
    {value: 'MALE', label: 'Male'},
    {value: 'FEMALE', label: 'Female'}
  ];

  drawMethods: any [] = [
    {value: 1, label: 'Snake'}
  ];

  private subscriptions: Subscription = new Subscription();

  constructor(private tournamentConfigService: TournamentConfigService) {

  }

  ngOnInit(): void {
    this.startTimes = [];
    const pipe: StartTimePipe = new StartTimePipe();
    for (let i = 8; i <= 21; i++) {
      const fullHour = pipe.transform(i);
      this.startTimes.push({startTime: i, startTimeText: fullHour});
      const halfPastHour = pipe.transform(i + 0.5);
      this.startTimes.push({startTime: (i + 0.5), startTimeText: halfPastHour});
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    const tournamentEventChange: SimpleChange = changes.tournamentEvent;
    if (tournamentEventChange != null) {
      this.tournamentEvent = tournamentEventChange.currentValue;
      const tournamentId = this.tournamentEvent?.tournamentFk;
      if (tournamentId != null) {
        this.initializeDaysChoices(tournamentId);
      }
    }
  }

  private initializeDaysChoices(tournamentId: number) {
    const selectedEntrySelector = createSelector(
      this.tournamentConfigService.selectors.selectEntityMap,
      (entityMap) => {
        return entityMap[tournamentId];
      });
    const selectedEntry$: Observable<Tournament> = this.tournamentConfigService.store.select(selectedEntrySelector);
    const subscription = selectedEntry$.subscribe((tournament: Tournament) => {
      // console.log('got tournament', tournament);
      if (tournament != null) {
        const startDate = tournament.startDate;
        const endDate = tournament.endDate || startDate;
        const tournamentDuration = new DateUtils().daysBetweenDates(startDate, endDate) + 1;
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
    this.subscriptions.add (subscription);
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
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
