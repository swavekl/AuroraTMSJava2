import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChange, SimpleChanges} from '@angular/core';
import {TableUsage} from '../model/table-usage.model';
import {MatchCard} from '../../matches/model/match-card.model';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {DrawType} from '../../draws/model/draw-type.enum';
import {Match} from '../../matches/model/match.model';
import {TableStatus} from '../model/table-status';

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
  printMatchCards: EventEmitter<number[]> = new EventEmitter<number[]>();

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
    // console.log('onStartEventMatches for event ', this.selectedEventId);
    const eventMatchCardIds: number [] = [];
    for (let i = 0; i < this.matchesToPlayInfos.length; i++) {
      const matchesToPlayInfo = this.matchesToPlayInfos[i];
      const matchCard = matchesToPlayInfo.matchCard;
      if (matchCard.eventFk === this.selectedEventId && matchCard.drawType === DrawType.ROUND_ROBIN) {
        eventMatchCardIds.push(matchCard.id);
      }
    }
    this.startSelectedMatchCards(eventMatchCardIds);
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

  onPrintEventMatchCards() {
    this.printMatchCards.emit(this.selectedMatchCardIds);
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
}

export class MatchInfo {
  matchCard: MatchCard;
  tournamentEvent: TournamentEvent;
}
