import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChange, SimpleChanges} from '@angular/core';
import {CdkDrag, CdkDragDrop, CdkDropList} from '@angular/cdk/drag-drop';
import {TableUsage} from '../model/table-usage.model';
import {MatchCard} from '../../matches/model/match-card.model';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {DrawType} from '../../draws/model/draw-type.enum';
import {Match} from '../../matches/model/match.model';
import {TableStatus} from '../model/table-status';
import {DateUtils} from '../../shared/date-utils';
import {MatchCardPlayabilityStatus, MatchInfo} from '../model/match-info.model';
import {MatchCardStatusPipe} from '../pipes/match-card-status.pipe';
import {MatSlideToggleChange} from '@angular/material/slide-toggle';
import {MatSelectChange} from '@angular/material/select/select';
import {MatchAssignmentDialogComponent, MatchAssignmentDialogData} from '../util/match-assignment-dialog.component';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';

@Component({
  selector: 'app-table-usage',
  templateUrl: './table-usage.component.html',
  styleUrls: ['./table-usage.component.scss']
})
export class TableUsageComponent implements OnInit, OnChanges {

  @Input()
  tableUsageList: TableUsage [];

  // match cards and event information for match cards available to be played
  @Input()
  matchesToPlayInfos: MatchInfo [];

  @Input()
  allTodaysMatchCards: MatchCard[];

  // day of tournament 1, 2 etc.
  @Input()
  tournamentDay: number;

  @Input()
  tournamentEvents: TournamentEvent[];

  @Output()
  printMatchCards: EventEmitter<any> = new EventEmitter<any>();

  @Output()
  startMatches: EventEmitter<TableUsage[]> = new EventEmitter<TableUsage[]>();

  @Output()
  refreshUsage: EventEmitter<any> = new EventEmitter<any>();

  public selectedEventId: number;

  public selectedMatchCardIds: number [];

  private matchCardStatusPipe: MatchCardStatusPipe = new MatchCardStatusPipe();

  // list of events which take place today
  todaysTournamentEvents: TournamentEvent[];

  hideOtherEventsMatches: boolean;

  filteredMatchInfos: MatchInfo [];

  constructor(private dialog: MatDialog) {
    this.selectedEventId = 0;
    this.selectedMatchCardIds = [];
    this.matchesToPlayInfos = [];
    this.tableUsageList = [];
    this.tournamentEvents = [];
    this.todaysTournamentEvents = [];
    this.filteredMatchInfos = [];
    this.hideOtherEventsMatches = false;
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    const tournamentEventChanges: SimpleChange = changes.tournamentEvents;
    if (tournamentEventChanges != null) {
      const tournamentEvents = tournamentEventChanges.currentValue;
      if (tournamentEvents) {
        this.todaysTournamentEvents = tournamentEvents.filter(tournamentEvent => tournamentEvent.day === this.tournamentDay);
      }
    }

    const matchInfosChanges: SimpleChange = changes.matchesToPlayInfos;
    if (matchInfosChanges != null) {
      const matchesToPlayInfos = matchInfosChanges.currentValue;
      if (matchesToPlayInfos != null) {
        this.filterMatchInfos(this.hideOtherEventsMatches, this.selectedEventId);
      }
    }
    // const tableUsageChange: SimpleChange = changes.tableUsageList;
    // if (tableUsageChange != null) {
    //   console.log('in presenter got table usage', tableUsageChange);
    // }
  }

  onSelectMatchCard(matchCard: MatchCard) {
    this.selectedMatchCardIds = [matchCard.id];
  }

  getTooltipText(matchInfo: MatchInfo): string {
    let tooltipText = '';
    const matchCard = matchInfo.matchCard;
    if (matchCard && matchCard.drawType === DrawType.SINGLE_ELIMINATION) {
      const matches: Match [] = matchCard.matches;
      if (matches && matchCard.profileIdToNameMap) {
        const theMatch = matches[0];
        const playerAName = (theMatch.playerAProfileId !== Match.TBD_PROFILE_ID)
          ? matchCard.profileIdToNameMap[theMatch.playerAProfileId] : Match.TBD_PROFILE_ID;
        const playerBName = (theMatch.playerBProfileId !== Match.TBD_PROFILE_ID)
          ? matchCard.profileIdToNameMap[theMatch.playerBProfileId] : Match.TBD_PROFILE_ID;
        const strMatchStatus = this.matchCardStatusPipe.transform(matchInfo.matchCardPlayability, matchInfo.playabilityDetail);
        tooltipText = `${playerAName} vs. ${playerBName} ${strMatchStatus}`;
      }
    }
    return tooltipText;
  }

  onStartEventMatches() {
    const eventRRMatchCardIds = this.getEventRRMatchCards();
    if (eventRRMatchCardIds.length > 0) {
      this.startSelectedMatchCards(eventRRMatchCardIds);
    }
  }

  private getEventRRMatchCards() {
    const eventMatchCardIds: number [] = [];
    for (let i = 0; i < this.matchesToPlayInfos.length; i++) {
      const matchesToPlayInfo = this.matchesToPlayInfos[i];
      const matchCard = matchesToPlayInfo.matchCard;
//      console.log('matchCard eventId ' + matchCard.eventFk + ' drawType ' + matchCard.drawType);
      if (matchCard.eventFk === this.selectedEventId && matchCard.drawType === DrawType.ROUND_ROBIN) {
        eventMatchCardIds.push(matchCard.id);
      }
    }
    return eventMatchCardIds;
  }

  onStartSelectedMatch() {
    this.startSelectedMatchCards(this.selectedMatchCardIds);
  }

  private startSelectedMatchCards(matchCardIds: number[]) {
    const unavailableTables: number [] = [];
    // find table numbers to update
    const startTime: Date = new Date();
    const updatedTableUsages: TableUsage [] = [];
    for (let i = 0; i < this.matchesToPlayInfos.length; i++) {
      const matchCard = this.matchesToPlayInfos[i].matchCard;
      for (const matchCardId of matchCardIds) {
        if (matchCardId === matchCard.id) {
          const assignedTables = matchCard.assignedTables;
          if (assignedTables) {
            const strTableNumbers: string [] = assignedTables.split(',');
            for (let j = 0; j < strTableNumbers.length; j++) {
              const tableNumber = Number(strTableNumbers[j]);
              for (let t = 0; t < this.tableUsageList.length; t++) {
                const tableUsage = this.tableUsageList[t];
                if (tableUsage.tableNumber === tableNumber) {
                  if (tableUsage.tableStatus !== TableStatus.Free) {
                    unavailableTables.push(tableNumber);
                  }
                  const updatedTableUsage: TableUsage = {
                    ...tableUsage,
                    tableStatus:  TableStatus.InUse,
                    matchStartTime: startTime,
                    matchCardFk: matchCardId
                  };
                  updatedTableUsages.push(updatedTableUsage);
                }
              }
            }
          }
        }
      }
    }

    if (unavailableTables.length > 0) {
      // show dialog to allow reassignment
      if (matchCardIds.length === 1) {
        const matchAssignmentDialogData = this.makeTableAssignmentDialogData(matchCardIds, unavailableTables);
        if (matchAssignmentDialogData) {
          const config: MatDialogConfig = {
            width: '380px', height: '300px', data: matchAssignmentDialogData
          };
          // show pairing dialog
          const dialogRef = this.dialog.open(MatchAssignmentDialogComponent, config);
          dialogRef.afterClosed().subscribe(result => {
            if (result.action === 'force') {
              this.startMatches.emit(updatedTableUsages);
            } else if (result.action === 'move') {
              const newTableNumbers = result.useTables;
              const matchCardId = matchCardIds[0];
              const forcedTableUsages: TableUsage [] = [];
              for (let i = 0; i < newTableNumbers.length; i++) {
                const tableNumber = newTableNumbers[i];
                const tableUsage = this.tableUsageList.find(tableUsage1 => tableUsage1.tableNumber === tableNumber);
                if (tableUsage) {
                  tableUsage.tableStatus = TableStatus.InUse;
                  tableUsage.matchStartTime = new Date();
                  tableUsage.matchCardFk = matchCardId;
                  tableUsage.completedMatches = 0;
                  tableUsage.totalMatches = 0;
                  forcedTableUsages.push(tableUsage);
                }
              }
              this.startMatches.emit(forcedTableUsages);
            }
          });
        }
      } else {
        // show warning if multiple match cards are started
      }
    } else {
      this.startMatches.emit(updatedTableUsages);
    }
  }

  private makeTableAssignmentDialogData(matchCardIds: number[], conflictTables: number[]): MatchAssignmentDialogData {
      const selectedMatchInfo = this.matchesToPlayInfos.find(matchInfo => matchInfo.matchCard.id === matchCardIds[0]);
      const availableTables: number [] = [];
      this.tableUsageList.forEach(tableUsage => {
        if (tableUsage.tableStatus === TableStatus.Free) {
          availableTables.push(tableUsage.tableNumber);
        }
      });
      return {
        availableTables: availableTables,
        conflictTables: conflictTables,
        matchCard: selectedMatchInfo.matchCard
      };
    }

    /**
   * Print single selected match card
   */
  onPrintSelectedMatch() {
    const printInfo = {
      eventId: null,
      matchCardIds: this.selectedMatchCardIds
    };
    this.printMatchCards.emit(printInfo);
  }

  /**
   * Print all RR round match cards for this event
   */
  onPrintEventMatchCards() {
    const eventRRMatchCardIds = this.getEventRRMatchCards();
    if (eventRRMatchCardIds.length > 0) {
      const printInfo = {
        eventId: this.selectedEventId,
        matchCardIds: eventRRMatchCardIds
      };
      this.printMatchCards.emit(printInfo);
    }
  }

  getTableTooltip(tableUsage: TableUsage): string {
    const matchCardId = tableUsage.matchCardFk;
    return this.getMatchIdentifierText(matchCardId);
  }

  public getMatchIdentifierText(matchCardId: number) {
    // console.log('getMatchIdentifierText ', matchCardId);
    let matchIdentifierText = '';
    if (this.allTodaysMatchCards && matchCardId !== 0) {
      for (let i = 0; i < this.allTodaysMatchCards.length; i++) {
        const matchCard = this.allTodaysMatchCards[i];
        if (matchCard.id === matchCardId) {
          const eventName = this.getEventName(matchCard.eventFk);
          matchIdentifierText = MatchCard.getFullMatchName(eventName, matchCard.drawType, matchCard.round, matchCard.groupNum);
          break;
        }
      }
      // console.log(matchCardId + ' -> ' + matchIdentifierText);
    }
    return matchIdentifierText;
  }

  isSelectedMatchCard(matchCard: MatchCard) {
    return this.selectedMatchCardIds.includes(matchCard.id);
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

  /**
   * Calculates percentage of match completion
   * @param tableUsage table usage
   */
  getPercentComplete(tableUsage: TableUsage): string {
    let percentComplete = '';
    if (tableUsage.tableStatus === TableStatus.InUse) {
      if (tableUsage.totalMatches !== 0 && tableUsage.completedMatches !== 0) {
        const percent = Math.floor((tableUsage.completedMatches / tableUsage.totalMatches) * 100);
        percentComplete = `${percent}%`;
      } else {
        percentComplete = '0%';
      }
    }
    return percentComplete;
  }

  getRunningTime(tableUsage: TableUsage): string {
    if (tableUsage.tableStatus === TableStatus.InUse) {
      const startTime = tableUsage.matchStartTime;
      const now = new Date();
      return new DateUtils().getTimeDifferenceAsString(startTime, now);
    } else {
      return '';
    }
  }

  exceededAllottedTime(tableUsage: TableUsage) {
    let exceeds = false;
    if (tableUsage.tableStatus === TableStatus.InUse) {
      if (this.allTodaysMatchCards) {
        for (let i = 0; i < this.allTodaysMatchCards.length; i++) {
          const matchCard = this.allTodaysMatchCards[i];
          if (matchCard.id === tableUsage.matchCardFk) {
            if (matchCard.duration !== 0) {
              const timeDifferenceInMinutes = new DateUtils().getTimeDifference(tableUsage.matchStartTime, new Date());
              const tableNumbersArray = (matchCard.assignedTables == null) ? [''] : (matchCard.assignedTables.split(','));
              const numTables = tableNumbersArray.length;
              const scheduledDurationInMinutes = (numTables > 0) ? matchCard.duration / numTables : matchCard.duration;
              exceeds = timeDifferenceInMinutes > scheduledDurationInMinutes;
              break;
            }
          }
        }
      }
    }
    return exceeds;
  }

  onStopMatch() {
    if (this.selectedMatchCardIds?.length === 1) {
      const matchCardId = this.selectedMatchCardIds[0];
      const updatedTableUsages = [];
      for (let t = 0; t < this.tableUsageList.length; t++) {
        const tableUsage = this.tableUsageList[t];
        if (tableUsage.matchCardFk === matchCardId) {
          tableUsage.tableStatus = TableStatus.Free;
          tableUsage.matchStartTime = null;
          tableUsage.matchCardFk = 0;
          updatedTableUsages.push(tableUsage);
        }
      }
      if (updatedTableUsages.length > 0) {
        this.startMatches.emit(updatedTableUsages);
      }
    }
  }

  selectUsedTable(tableUsage: TableUsage) {
    const matchCardFk = tableUsage.matchCardFk;
    if (matchCardFk !== 0) {
      this.selectedMatchCardIds = [matchCardFk];
    } else {
      this.selectedMatchCardIds = [];
    }
  }

  isTableSelected(tableUsage: TableUsage) {
    const matchCardFk = tableUsage.matchCardFk;
    if (matchCardFk !== 0) {
      return this.selectedMatchCardIds?.indexOf(matchCardFk) !== -1;
    } else {
      return false;
    }
  }

  getMatchTables(assignedTables: string) {
    if (assignedTables != null) {
      const oneTable: boolean = (assignedTables.indexOf(',') === -1);
      return (oneTable) ? `table: ${assignedTables}` : `tables: ${assignedTables}`;
    } else {
      return '(table is not assigned)';
    }
  }

  isLinkedOnLeft(tableUsage: TableUsage): boolean {
    let isOnLeft = false;
    const assignedTables = this.getAssignedTables(tableUsage);
    if (assignedTables.length > 1) {
      for (let i = 0; i < assignedTables.length; i++) {
        const assignedTable = assignedTables[i];
        if (tableUsage.tableNumber === assignedTable) {
          // is it a subsequent table in the list?
          isOnLeft = (i > 0);
        }
      }
    }
    return isOnLeft;
  }

  isLinkedOnRight(tableUsage: TableUsage): boolean {
    let isOnRight = false;
    const assignedTables = this.getAssignedTables(tableUsage);
    if (assignedTables.length > 1) {
      for (let i = 0; i < assignedTables.length; i++) {
        const assignedTable = assignedTables[i];
        if (tableUsage.tableNumber === assignedTable) {
          // is it any but last one in the list
          isOnRight = (i < (assignedTables.length - 1));
        }
      }
    }
    return isOnRight;
  }

  private getAssignedTables(tableUsage: TableUsage): number[] {
    let assignedTables: number[] = [];
    if (tableUsage.tableStatus === TableStatus.InUse) {
      if (this.allTodaysMatchCards) {
        for (let i = 0; i < this.allTodaysMatchCards.length; i++) {
          const matchCard = this.allTodaysMatchCards[i];
          if (matchCard.id === tableUsage.matchCardFk) {
            const strAssignedTables = matchCard.assignedTables;
            const strAssignedTablesArray = strAssignedTables.split(',');
            assignedTables = strAssignedTablesArray.map((strAssignedTable => Number(strAssignedTable)));
            break;
          }
        }
      }
    }
    return assignedTables;
  }

  getStatusClass(matchCardPlayability: MatchCardPlayabilityStatus) {
    if (matchCardPlayability === MatchCardPlayabilityStatus.ReadyToPlay) {
      return 'match-status-ready';
    } else {
      return 'match-status-waiting';
    }
  }

  /**
   * Manually triggered refresh
   */
  onRefresh() {
    this.refreshUsage.emit('');
  }

  onHideOtherEvents($event: MatSlideToggleChange) {
    const hide = $event.checked;
    this.filterMatchInfos(hide, this.selectedEventId);
  }

  private filterMatchInfos(hide: boolean, eventId: number) {
    // console.log(`filterMatchInfos ${eventId} hide ${hide}`);
    if (hide === true && eventId !== 0) {
      this.filteredMatchInfos = this.matchesToPlayInfos.filter((matchInfo) => matchInfo.matchCard.eventFk === eventId);
    } else {
      this.filteredMatchInfos = this.matchesToPlayInfos;
    }
  }

  eventSelectionChange($event: MatSelectChange) {
    // console.log('event changed', $event);
    const eventId = $event.value;
    this.filterMatchInfos(this.hideOtherEventsMatches, eventId);
  }

  isSelectedMatchReady() {
    let finalResult = false;
    if (this.selectedMatchCardIds?.length === 1) {
      let selectedMatchReady = false;
      const selectedMatchCardId = this.selectedMatchCardIds[0];
      this.matchesToPlayInfos.forEach(matchInfo => {
        if (matchInfo.matchCard.id === selectedMatchCardId) {
          selectedMatchReady = (matchInfo.matchCardPlayability === MatchCardPlayabilityStatus.ReadyToPlay);
        }
      });
      finalResult = selectedMatchReady;
    }
    return finalResult;
  }

  /**
   * Finds if given table is free
   * @param assignedTables
   */
  isTableFree(assignedTables: string) {
    let isTableFree = true;
    if (assignedTables != null) {
      const strTableNumbers: string [] = assignedTables.split(',');
      const tableNumbers: number [] = strTableNumbers.map((strTableNumber: string) => Number(strTableNumber));
      this.tableUsageList.forEach(tableUsage => {
        const usedTableNum = tableUsage.tableNumber;
        if (tableNumbers.includes(usedTableNum) && tableUsage.tableStatus === TableStatus.InUse) {
          isTableFree = false;
        }
      });
    }

    return isTableFree;
  }

  /**
   * Drag and drop support for table reassignment
   */
    canMoveMatchInfo(item: CdkDrag<MatchInfo>) {
      return (item.data?.matchCardPlayability === MatchCardPlayabilityStatus.ReadyToPlay);
    }

  /**
   *
   * @param index index of item being dropped
   * @param item item being dropped
   * @param drop drop target
   */
    canDropPredicate(index: number, item: CdkDrag<TableUsage>, drop: CdkDropList) {
    console.log('canDropPredicate', drop);
    return item.data.tableStatus === TableStatus.Free;
  }

  /**
   * Callback when match info is being dropped
   * @param $event
   */
  onMatchInfoDrop($event: CdkDragDrop<TableUsage>) {
    console.log('in onMatchInfoDrop', $event);
  }
}


