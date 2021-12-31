import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {TableUsage} from '../model/table-usage.model';
import {MatchCard} from '../../matches/model/match-card.model';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {DrawType} from '../../draws/model/draw-type.enum';
import {Match} from '../../matches/model/match.model';
import {TableStatus} from '../model/table-status';
import {DateUtils} from '../../shared/date-utils';
import {MatchInfo} from '../model/match-info.model';

@Component({
  selector: 'app-table-usage',
  templateUrl: './table-usage.component.html',
  styleUrls: ['./table-usage.component.scss']
})
export class TableUsageComponent implements OnInit, OnChanges {

  @Input()
  tableUsageList: TableUsage [] = [];

  // match cards and event information for match cards available to be played
  @Input()
  matchesToPlayInfos: MatchInfo [];

  @Input()
  allTodaysMatchCards: MatchCard[];

  @Input()
  tournamentEvents: TournamentEvent[];

  @Output()
  printMatchCards: EventEmitter<any> = new EventEmitter<any>();

  @Output()
  startMatches: EventEmitter<TableUsage[]> = new EventEmitter<TableUsage[]>();

  public selectedEventId: number;

  public selectedMatchCardIds: number [];

  constructor() {
    this.selectedEventId = 0;
    this.selectedMatchCardIds = [];
    this.matchesToPlayInfos = [];
    this.tableUsageList = [];
    this.tournamentEvents = [];
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    // const tableUsageListChange: SimpleChange = changes.tableUsageList;
    // if (tableUsageListChange?.currentValue != null) {
    //   console.log('COMP got tableUsageList', tableUsageListChange.currentValue);
    //   const tableUsageList: TableUsage[] = tableUsageListChange.currentValue;
    // }
    //
    // const tournamentEventsChange: SimpleChange = changes.tournamentEvents;
    // if (tournamentEventsChange?.currentValue != null) {
    //   const tournamentEvents = tournamentEventsChange.currentValue;
    //   console.log('COMP got tournamentEvents', tournamentEvents);
    // }
    //
    // const matchesToPlayInfosChange: SimpleChange = changes.matchesToPlayInfos;
    // if (matchesToPlayInfosChange?.currentValue != null) {
    //   const matchesToPlayInfos = matchesToPlayInfosChange.currentValue;
    //   console.log('COMP got matchesToPlayInfos', matchesToPlayInfos);
    // }
  }

  onSelectMatchCard(matchCard: MatchCard) {
    this.selectedMatchCardIds = [matchCard.id];
  }

  getRoundShortName(round: number): string {
    return MatchCard.getRoundShortName(round);
  }

  getTooltipText(matchInfo: MatchInfo): string {
    let tooltipText = '';
    const matchCard = matchInfo.matchCard;
    if (matchCard && matchCard.drawType === DrawType.SINGLE_ELIMINATION) {
      const matches: Match [] = matchCard.matches;
      if (matches && matchCard.profileIdToNameMap) {
        const theMatch = matches[0];
        const playerAName = matchCard.profileIdToNameMap[theMatch.playerAProfileId];
        const playerBName = matchCard.profileIdToNameMap[theMatch.playerBProfileId];
        tooltipText = `${playerAName} vs. ${playerBName}`;
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
    console.log('startSelectedMatchCards', matchCardIds);
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
                  tableUsage.tableStatus = TableStatus.InUse;
                  tableUsage.matchStartTime = startTime;
                  tableUsage.matchCardFk = matchCardId;
                  updatedTableUsages.push(tableUsage);
                }
              }
            }
          }
        }
      }
    }

    this.startMatches.emit(updatedTableUsages);
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
          const groupNumber = matchCard.groupNum;
          if (matchCard.drawType === 'ROUND_ROBIN') {
            matchIdentifierText = `${eventName} R.R. Group ${groupNumber}`;
          } else {
            const roundName = this.getRoundShortName(matchCard.round);
            matchIdentifierText = `${eventName} ${roundName} M ${groupNumber}`;
          }
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
      return (oneTable) ? `Table: ${assignedTables}` : `Tables: ${assignedTables}`;
    } else {
      return 'Table: Not assigned';
    }
  }

  isLinkedOnLeft(tableUsage: TableUsage): boolean {
    let isOnLeft = false;
    const assignedTables = this.getAssignedTables(tableUsage);
    if (assignedTables != null && assignedTables.length > 1) {
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
    if (assignedTables != null && assignedTables.length > 1) {
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
    let assignedTables = null;
    if (tableUsage.tableStatus === TableStatus.InUse) {
      if (this.allTodaysMatchCards) {
        for (let i = 0; i < this.allTodaysMatchCards.length; i++) {
          const matchCard = this.allTodaysMatchCards[i];
          if (matchCard.id === tableUsage.matchCardFk) {
            const strAssignedTables = matchCard.assignedTables;
            const strAssignedTablesArray = strAssignedTables.split(',');
            assignedTables = [];
            for (let j = 0; j < strAssignedTablesArray.length; j++) {
              const assignedTableNumber = Number(strAssignedTablesArray[j]);
              assignedTables.push(assignedTableNumber);
            }
            break;
          }
        }
      }
    }
    return assignedTables;
  }
}


