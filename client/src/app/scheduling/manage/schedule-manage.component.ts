import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChange, SimpleChanges} from '@angular/core';
import {Tournament} from '../../tournament/tournament-config/tournament.model';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {DateUtils} from '../../shared/date-utils';
import {EventDayPipePipe} from '../../shared/pipes/event-day-pipe.pipe';
import {MatchCard} from '../../matches/model/match-card.model';
import {DisplayGrid, Draggable, GridsterConfig, GridsterItem, GridsterItemComponentInterface, GridType} from 'angular-gridster2';
import {Subject, Subscription} from 'rxjs';
import {debounceTime} from 'rxjs/operators';

interface Safe extends GridsterConfig {
  draggable: Draggable;
}

@Component({
  selector: 'app-schedule-manage',
  templateUrl: './schedule-manage.component.html',
  styleUrls: ['./schedule-manage.component.scss']
})
export class ScheduleManageComponent implements OnInit, OnChanges, OnDestroy {

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

  @Output()
  public updateMatchCardsEvent: EventEmitter<MatchCard[]> = new EventEmitter<MatchCard[]>();

  // days
  days: any [] = [];

  // selected tournament day for which to show the events
  selectedDay: number;

  // array to be used for *ngFor interation
  startingTimes: any [] = [];

  // array to be used for *ngFor iteration
  tablesArray: any [] = [];

  eventColors: string [] = [
    '#98FB98', '#EAF509', '#FFE4B5', '#00BFFF',
    '#DDA0DD', '#40E0D0', '#FF6347', '#87CEFA',
    '#7E95CD', '#daa520', '#9370DB', '#D3D3D3',
    '#bada55', '#ffc0cb', '#fff68f', '#66CDAA'
  ];

  // index of item over which we are currently hovering, since it is not correctly computed
  private targetItemTableNum: number;
  private targetItemIndex: number;
  private sourceItemIndex: number;
  // debug
  private fromTableNumber: number;
  private fromTime: number;
  // debug information
  movementDetail: string;

  // options for gridster2 component
  gridsterOptions: Safe;

  // items for populating the grid
  gridsterItems: Array<GridsterItem> = [];

  // match cards which have changed - collection is rebuilt during each drag and drop sequence
  changedMatchCards: MatchCard [] = [];

  // subject to signal that another match card has changed
  changeSubject = new Subject<number>();

  // subscriptions for this component
  subscriptions: Subscription = new Subscription();

  constructor() {
    this.startingTimes = new DateUtils().getEventStartingTimes();
    this.selectedDay = 1;
    this.targetItemIndex = -1;
    this.targetItemTableNum = -1;
    this.sourceItemIndex = -1;
    this.fromTableNumber = 0;
    this.fromTime = 0;
  }

  ngOnInit(): void {
    this.gridsterOptions = this.buildGridsterOptions();
    this.setupUpdateSubscription();
  }

  private buildGridsterOptions(): Safe {
    return {
      gridType: GridType.Fixed,
      fixedColWidth: 60,
      fixedRowHeight: 60,
      maxCols: 29,
      displayGrid: DisplayGrid.Always,
      pushItems: true,  // we can either push or switch - push is more useful
      swap: false,
      margin: 0,
      itemChangeCallback: this.itemChange,
      draggable: {
        delayStart: 0,
        enabled: true,
        ignoreContentClass: 'gridster-item-content',
        ignoreContent: false,
        dragHandleClass: 'drag-handler',
        // stop: this.eventStop,
        start: this.eventStart,
        dropOverItems: false,
        // dropOverItemsCallback: this.overlapEvent,
      },
      resizable: {
        enabled: false
      }
    };
  }

  ngOnDestroy() {
    this.subscriptions.unsubscribe();
  }

  ngOnChanges(changes: SimpleChanges): void {
    const tournamentChanges: SimpleChange = changes.tournament;
    if (tournamentChanges != null) {
      const tournament = tournamentChanges.currentValue;
      if (tournament != null) {
        const numTables = tournament.configuration.numberOfTables || 1;
        this.tablesArray = Array(numTables);
        this.gridsterOptions = {...this.gridsterOptions, maxRows: numTables};
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
      this.gridsterItems = this.makeGridsterItems(this.matchCards);
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

  private makeGridsterItems (matchCards: MatchCard[]): GridsterItem[] {
    const gridsterItems: Array<GridsterItem> = [];
    const numTables = this.tournament?.configuration?.numberOfTables ?? 0;
    if (numTables > 0) {
      // gridsterItems.push({cols: 1, rows: 1, y: 0, x: 0,
      //   dragEnabled: false, resizeEnabled: false,
      //   label: 'Table #', isHeader: true});
      // for (let i = 0; i < this.startingTimes.length; i++) {
      //   gridsterItems.push({cols: 1, rows: 1, y: 0, x: (i + 1),
      //     dragEnabled: false, resizeEnabled: false, isHeader: true, isTableNum: false,
      //     label: this.startingTimes[i].startTimeText});
      // }
      // for (let tableNum = 1; tableNum <= numTables; tableNum++) {
      for (let tableNum = 0; tableNum < numTables; tableNum++) {
        gridsterItems.push({cols: 1, rows: 1, y: tableNum, x: 0,
          dragEnabled: false, resizeEnabled: false, isHeader: true, isTableNum: true,
          label: `${tableNum + 1}`});
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

      // convert match cards into gridster items
      matchCards.forEach((matchCard: MatchCard) => {
        const assignedTables = matchCard.assignedTables;
        // console.log(matchCard.id + ' assignedTables ' + assignedTables + ' time ' + matchCard.startTime);
        if (assignedTables != null && assignedTables !== '') {
          const strTableNums: string [] = assignedTables.split(',');
          const firstTableNum = (strTableNums.length > 0) ? Number(strTableNums[0]) - 1 : 0;
          const duration = matchCard.duration;
          const rowSpan = strTableNums.length;
          const colSpan = (duration / 30);
          const startTime = matchCard.startTime;
          const endTime = startTime + (0.5 * colSpan);
          const eventColor = eventToColorMap[matchCard.eventFk];
          const eventName = this.getEventName(matchCard.eventFk);
          const x = ((startTime - this.startingTimes[0].startTime) / 0.5) + 1;
          gridsterItems.push({cols: colSpan, rows: rowSpan, y: firstTableNum, x: x,
            dragEnabled: true, resizeEnabled: false, isHeader: false,
            label: eventName, backgroundColor: eventColor,
            groupNum: matchCard.groupNum, round: matchCard.round, matchCard: matchCard,
            scheduleManageComponent: this});
        }
      });
    }

    return gridsterItems;
  }

  /**
   * Start of drag event
   * @param item
   * @param itemComponent
   * @param event
   */
  eventStart(item: GridsterItem, itemComponent: GridsterItemComponentInterface, event: MouseEvent): void {
    // console.log('eventStart', item, itemComponent, event);
    const me = item['scheduleManageComponent'];
    me.changedMatchCards = [];
  }

  /**
   * Called for each item that has changed.  There will be multiple calls if items were pushed around
   * @param item
   * @param itemComponent
   */
  itemChange (item: GridsterItem, itemComponent: GridsterItemComponentInterface): void {
    const me = item['scheduleManageComponent'];
    me.itemChangeInternal(item, itemComponent);
  }

  private itemChangeInternal(item: GridsterItem, itemComponent: GridsterItemComponentInterface) {
    // console.log ('itemChange', item);
    // we are called here successively but we don't know which call is the last one in this drag and drop sequence
    // collect updated match cards in changedMatchCards array
    const matchCard: MatchCard = item['matchCard'];
    const newStartTableNum = item.y + 1;
    const newTableNumbers: string = this.generateUpdatedTableNumbers(matchCard, newStartTableNum);
    const newStartTime = ((item.x - 1) * 0.5) + 8.0;
    const updatedMatchCard = {...matchCard, assignedTables: newTableNumbers, startTime: newStartTime};
    this.changedMatchCards.push(updatedMatchCard);

    // signal that we have another updated match card.  the subscriber will wait for another one up to 500ms
    // and if no new ones arrive then it means this was the last one and we are ready to update all of them
    this.changeSubject.next(this.changedMatchCards.length);
  }

  private setupUpdateSubscription () {
    // wait for the last changed item to be collected and only then send them all for update
    this.subscriptions = this.changeSubject
      .pipe(debounceTime(500))
      .subscribe(numChangedCards => {
        this.updateMatchCardsEvent.emit(this.changedMatchCards);
      });
  }

  private generateUpdatedTableNumbers(matchCard: MatchCard, newTableNumber: number): string {
    const assignedTables: string [] = matchCard.assignedTables.split(',');
    const assignedTableNumbers: number [] = [];
    for (let i = 0; i < assignedTables.length; i++) {
      assignedTableNumbers.push(newTableNumber);
      newTableNumber++;
    }
    return assignedTableNumbers.join(',');
  }

  getItemClass(item: GridsterItem) {
    return (item.round === 0) ? 'round-round-robin' : ((item.round > 16) ? 'round-preliminary' : 'round-of-' + item.round);
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

  getRoundGroupLabel(groupNum: number, round: number) {
    const seMatchIdentifier: string = (round > 2)
      ? `${MatchCard.getRoundAbbreviatedName(round)} M ${groupNum}`
      : ((groupNum === 1) ? 'Final' : '3rd & 4th');
    return (round === 0)
      ? `RR Group ${groupNum}`
      : `${seMatchIdentifier}`;
  }
}
