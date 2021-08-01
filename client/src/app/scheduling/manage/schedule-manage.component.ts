import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChange, SimpleChanges} from '@angular/core';
import {Tournament} from '../../tournament/tournament-config/tournament.model';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {DateUtils} from '../../shared/date-utils';
import {EventDayPipePipe} from '../../shared/pipes/event-day-pipe.pipe';
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
  public tournamentEvents: TournamentEvent [] = [];

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

  // map of table number to schedule for this table
  scheduleTimeBlocks: any = {};

  eventColors: string [] = [
    '#8a2be2', '#00ffff', '#ffff00', '#f08080',
    '#f7347a', '#666666', '#00ff00', '#c6e2ff',
    '#bada55', '#ffc0cb', '#fff68f', '#990000',
    '#000080', '#daa520'
  ];

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
    const tournamentEventChanges: SimpleChange = changes.tournamentEvents;
    if (tournamentEventChanges != null) {
      const tournamentEvents = tournamentEventChanges.currentValue;
      if (tournamentEvents != null) {
        this.tournamentEvents = tournamentEvents;
      }
    }
    const matchCardChanges: SimpleChange = changes.matchCards;
    if (matchCardChanges != null) {
      const matchCards = matchCardChanges.currentValue;
      if (matchCards != null) {
        this.matchCards = matchCards;
      }
    }

    if (this.matchCards.length > 0 && this.tournamentEvents.length > 0) {
      this.scheduleTimeBlocks = this.buildScheduleArray(this.matchCards, this.tournamentEvents);
    }
  }

  /**
   *
   * @param tournament
   * @private
   */
  private buildTournamentDaysOptions(tournament: Tournament) {
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
    this.dayChangedEvent.emit(this.selectedDay);
  }

  onGenerateSchedule() {
    this.generateScheduleForEvent.emit(this.selectedDay);
  }

  /**
   *
   * @param matchCards
   * @private
   */
  private buildScheduleArray(matchCards: MatchCard[], tournamentEvents: TournamentEvent[]): any {
    const newSchedule = {};
    const numTables = this.tournament?.configuration?.numberOfTables ?? 0;
    if (numTables > 0) {
      const timeSlots = (22.0 - 8.0) * 2;
      // fill schedule with empty time slots of 1 x 1 (1 table by 30 minutes)
      for (let tableNum = 1; tableNum <= numTables; tableNum++) {
        const tableSchedule = [];
        newSchedule[tableNum] = tableSchedule;
        for (let timeSlot = 0; timeSlot < timeSlots; timeSlot++) {
          const time = 8.0 + (timeSlot * 0.5);
          tableSchedule.push(new ScheduleTimeBlock(tableNum, time, 1, 1, 0,
            '#ffffff', '', 0, 0));
        }
      }
      let colorIndex = 0;
      const eventToColorMap = {};
      matchCards.forEach((matchCard: MatchCard) => {
        const eventColor = eventToColorMap[matchCard.eventFk];
        if (eventColor == null) {
          eventToColorMap[matchCard.eventFk] = this.eventColors[colorIndex];
          colorIndex++;
          colorIndex = (colorIndex < this.eventColors.length) ? colorIndex : 0;
        }
      });

        // convert match cards into schedule
      matchCards.forEach((matchCard: MatchCard) => {
        const assignedTables = matchCard.assignedTables;
        const strTableNums: string [] = (assignedTables != null) ? assignedTables.split(',') : ['1'];
        const firstTableNum = Number(strTableNums[0]);
        const duration = matchCard.duration;
        const rowSpan = strTableNums.length;
        const colSpan = (duration / 30);
        const startTime = matchCard.startTime;
        const endTime = startTime + (0.5 * colSpan);
        const eventColor = eventToColorMap[matchCard.eventFk];
        let firstTable = true;
        for (let i = 0; i < strTableNums.length; i++) {
          const tableNum = Number(strTableNums[i]);
          const tableSchedule = newSchedule[tableNum];
          // console.log('tableNum ' + tableNum);
          const slotsToRemove: number [] = [];
          for (let j = 0; j < tableSchedule.length; j++) {
            const scheduleTimeBlock: ScheduleTimeBlock = tableSchedule[j];
            // console.log('table # ' + scheduleTimeBlock.tableNum + ' startTime ' + scheduleTimeBlock.time);
            if (startTime <= scheduleTimeBlock.time && scheduleTimeBlock.time < endTime) {
              slotsToRemove.push(j);
            }
          }

          // remove the empty time slots
          // go from the end
          // console.log('Removing ' + slotsToRemove.length + ' slots for event ' + this.getEventName(matchCard.eventFk));
          for (let k = (slotsToRemove.length - 1); k >= 0; k--) {
            const index = slotsToRemove[k];
            tableSchedule.splice(index, 1);
          }

          // place the match cards in place of first time slot
          if (firstTable) {
            const insertionIndex = (slotsToRemove.length > 0) ? slotsToRemove[0] : -1;
            if (insertionIndex >= 0) {
              const eventName = this.getEventName(matchCard.eventFk);
              // console.log('eventName', eventName);
              const matchCardScheduleTimeBlock = new ScheduleTimeBlock(firstTableNum,
                startTime, rowSpan, colSpan, matchCard.id, eventColor, eventName, matchCard.groupNum, matchCard.round);
              tableSchedule.splice(insertionIndex, 0, matchCardScheduleTimeBlock);
            }
          }
          firstTable = false;
        }

      });
    }
    return newSchedule;
  }

  private getEventName(eventFk: number) {
    for (let i = 0; i < this.tournamentEvents.length; i++) {
      const tournamentEvent = this.tournamentEvents[i];
      if (tournamentEvent.id === eventFk) {
        return tournamentEvent.name;
      }
    }
    return '';
  }

  getRoundGroup(scheduleTimeBlock: ScheduleTimeBlock) {
    return (scheduleTimeBlock.round === 0) ? `RR Group ${scheduleTimeBlock.groupNum}` : `SE Round of ${scheduleTimeBlock.round} / M 1`;
  }
}

export class ScheduleTimeBlock {
  rowSpan: number;
  colSpan: number;
  matchCardId: number;
  tableNum: number;
  time: number;
  color: string;
  eventName: string;
  groupNum: number;
  round: number;

  constructor(tableNum: number,
              time: number,
              rowSpan: number,
              colSpan: number,
              matchCardId: number,
              color: string,
              eventName: string,
              groupNum: number,
              round: number) {
    this.time = time;
    this.tableNum = tableNum;
    this.rowSpan = rowSpan;
    this.colSpan = colSpan;
    this.matchCardId = matchCardId;
    this.color = color;
    this.eventName = eventName;
    this.groupNum = groupNum;
    this.round = round;
  }
}
