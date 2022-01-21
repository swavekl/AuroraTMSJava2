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
import {EventDayPipePipe} from '../../../shared/pipes/event-day-pipe.pipe';
import {createSelector} from '@ngrx/store';
import {Observable, Subscription} from 'rxjs';
import {DateUtils} from '../../../shared/date-utils';
import {TournamentConfigService} from '../tournament-config.service';
import {Tournament} from '../tournament.model';
import {AgeRestrictionType} from '../model/age-restriction-type.enum';
import {MatSelectChange} from '@angular/material/select/select';
import {DrawMethod} from '../model/draw-method.enum';
import {PrizeInfo} from '../model/prize-info.model';
import {CommonRegexPatterns} from '../../../shared/common-regex-patterns';
import {TournamentEventConfiguration} from '../model/tournament-event-configuration.model';
import {PrizeInfoDialogComponent, PrizeInfoDialogData} from './prize-info-dialog/prize-info-dialog.component';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';

@Component({
  selector: 'app-tournament-event-config',
  templateUrl: './tournament-event-config.component.html',
  styleUrls: ['./tournament-event-config.component.css']
  // changeDetection: ChangeDetectionStrategy.OnPush
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

  readonly PRICE_REGEX = CommonRegexPatterns.PRICE_REGEX;

  readonly NUMERIC_WITH_ZERO_REGEX = CommonRegexPatterns.NUMERIC_WITH_ZERO_REGEX;
  readonly NUMERIC_REGEX = CommonRegexPatterns.NUMERIC_REGEX;
  readonly TWO_DIGIT_NUMERIC_REGEX = CommonRegexPatterns.TWO_DIGIT_NUMERIC_REGEX;

  columnsToDisplay: string[] = [];

  // gender restrictions
  genderRestrictions: any [] = [
    {value: 'NONE', label: 'None'},
    {value: 'MALE', label: 'Male'},
    {value: 'FEMALE', label: 'Female'}
  ];

  ageRestrictionTypes: any [] = [
    {value: 'NONE', label: 'None'},
    {value: 'AGE_UNDER_OR_EQUAL_ON_DAY_EVENT', label: 'Maximum age on day of tournament'},
    {value: 'AGE_OVER_AT_THE_END_OF_YEAR', label: 'Minimum age at the end of year'},
    {value: 'BORN_ON_OR_AFTER_DATE', label: 'Born on or before date'}
  ];

  maxAgeRestrictionDate: Date;
  minAgeRestrictionDate: Date;
  ageRestrictionDateEnabled: boolean;

  drawMethods: any [] = [
    {value: DrawMethod.SNAKE.valueOf(), label: 'Snake'},
    {value: DrawMethod.DIVISION.valueOf(), label: 'Division'},
    {value: DrawMethod.BY_RECORD.valueOf(), label: 'By Record'}
  ];

  // choices of number of games in a match
  numberOfGamesChoices: any [] = [
    {value: 3, label: 'Best of 3'},
    {value: 5, label: 'Best of 5'},
    {value: 7, label: 'Best of 7'}
  ];

  private subscriptions: Subscription = new Subscription();

  constructor(private tournamentConfigService: TournamentConfigService,
              private dialog: MatDialog) {
  }

  ngOnInit(): void {
    this.startTimes = new DateUtils().getEventStartingTimes();
  }

  ngOnChanges(changes: SimpleChanges): void {
    const tournamentEventChange: SimpleChange = changes.tournamentEvent;
    if (tournamentEventChange != null) {
      let tournamentEvent = tournamentEventChange.currentValue;
      if (tournamentEvent != null) {
        tournamentEvent = JSON.parse(JSON.stringify(tournamentEvent));
        // sort prize information
        const prizeInfoList = tournamentEvent?.configuration?.prizeInfoList ?? [];
        prizeInfoList.sort((left: PrizeInfo, right: PrizeInfo) => {
          if (tournamentEvent.drawMethod === DrawMethod.DIVISION) {
            return (left.division === right.division)
              ? (left.awardedForPlace > right.awardedForPlace ? 1 : -1)
              : (left.division > right.division ? 1 : -1) ;
          } else {
            return (left.awardedForPlace === right.awardedForPlace) ? 0
              : (left.awardedForPlace > right.awardedForPlace ? 1 : -1);
          }
        });
        this.tournamentEvent = tournamentEvent;

        this.ageRestrictionDateEnabled = (this.tournamentEvent.ageRestrictionType === AgeRestrictionType.BORN_ON_OR_AFTER_DATE);
        this.columnsToDisplay = (this.tournamentEvent.drawMethod === 'DIVISION')
          ? ['division', 'awardedForPlace', 'prizeMoneyAmount', 'awardTrophy', 'actions']
          : [            'awardedForPlace', 'prizeMoneyAmount', 'awardTrophy', 'actions'];
      }
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

        this.maxAgeRestrictionDate = dateUtils.getMaxAgeRestrictionDate(startDate);
        this.minAgeRestrictionDate = dateUtils.getMinAgeRestrictionDate(startDate);
        console.log('maxAgeRestrictionDate', this.maxAgeRestrictionDate);
        console.log('minAgeRestrictionDate', this.minAgeRestrictionDate);
      }
    });
    this.subscriptions.add(subscription);
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  onCancel() {
    this.canceled.emit('cancel');
  }

  onSave(formValues: TournamentEvent) {
    this.saved.emit(this.tournamentEvent);
  }

  isAgeRestrictionRequired(tournamentEvent: TournamentEvent) {
    return tournamentEvent?.ageRestrictionType === AgeRestrictionType.BORN_ON_OR_AFTER_DATE;
  }

  onAgeRestrictionChange($event: MatSelectChange) {
    this.ageRestrictionDateEnabled = ($event?.value === AgeRestrictionType.BORN_ON_OR_AFTER_DATE);
  }

  onAddPrizesRow() {
    this.onEdit(-1);
  }

  onRemovePrizesRow(indexToRemove: number) {
    console.log('indexToRemove', indexToRemove);
    const cloneTE: TournamentEvent = JSON.parse(JSON.stringify(this.tournamentEvent));
    const prizeInfos = cloneTE.configuration.prizeInfoList;
    prizeInfos.splice(indexToRemove, 1);
    cloneTE.configuration.prizeInfoList = prizeInfos;
    this.tournamentEvent = cloneTE;
  }

  onEdit(indexToEdit: number) {
    const prizeInfos = this.tournamentEvent.configuration.prizeInfoList;
    const prizeInfo: PrizeInfo = (indexToEdit >= 0) ? prizeInfos[indexToEdit] : new PrizeInfo();
    console.log('editing prize info at index ' + indexToEdit + ' => ' + prizeInfo);
    const prizeInfoDialogData: PrizeInfoDialogData = {
      drawMethod: this.tournamentEvent.drawMethod,
      prizeInfo: prizeInfo
    };
    const config: MatDialogConfig = {
      height: '320px', width: '470px', data: prizeInfoDialogData
    };
    const me = this;
    const dialogRef = this.dialog.open(PrizeInfoDialogComponent, config);
    dialogRef.afterClosed().subscribe(result => {
      if (result.action === 'ok') {
          const cloneTE: TournamentEvent = JSON.parse(JSON.stringify(me.tournamentEvent));
          const configuration = cloneTE.configuration || new TournamentEventConfiguration();
          const prizeInfoList = configuration.prizeInfoList || [];
          if (indexToEdit >= 0) {
            prizeInfoList.splice(indexToEdit, 1, result.prizeInfo);
          } else {
            prizeInfoList.push(result.prizeInfo);
          }
          configuration.prizeInfoList = prizeInfoList;
          cloneTE.configuration = configuration;
          me.tournamentEvent = cloneTE;
      }
    });
  }

  formatPlace(prizeInfo: PrizeInfo) {
    if (prizeInfo.awardedForPlaceRangeEnd != null && prizeInfo.awardedForPlaceRangeEnd > 0) {
      return `${prizeInfo.awardedForPlace} - ${prizeInfo.awardedForPlaceRangeEnd}`;
    } else {
      return `${prizeInfo.awardedForPlace}`;
    }
  }
}
