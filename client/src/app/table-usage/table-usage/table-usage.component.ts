import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
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

  @Input()
  matchCards: MatchCard[];

  @Input()
  tournamentEvents: TournamentEvent[];

  @Output()
  printMatchCards: EventEmitter<number[]> = new EventEmitter<number[]>();

  @Output()
  startMatches: EventEmitter<TableUsage[]> = new EventEmitter<TableUsage[]>();

  public matchesToPlayInfos: MatchInfo [];

  public selectedEventId: number;

  public selectedMatchCardIds: number [];

  constructor() {
    this.selectedEventId = 0;
    this.selectedMatchCardIds = [];
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    console.log('changes received');
    if (this.matchCards != null && this.tournamentEvents != null) {
      const matchesToPlay: any[] = [];
      for (const matchCard of this.matchCards) {
        const eventFk = matchCard.eventFk;
        for (const tournamentEvent of this.tournamentEvents) {
          if (tournamentEvent.id === eventFk) {
            const matchInfo: MatchInfo = {
              matchCard: matchCard,
              tournamentEvent: tournamentEvent
            };
            matchesToPlay.push(matchInfo);
            break;
          }
        }
      }
      this.matchesToPlayInfos = matchesToPlay;
    }
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
    const eventMatchCardIds: number [] = [];
    for (let i = 0; i < this.matchCards.length; i++) {
      const matchCard = this.matchCards[i];
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
    // find table numbers to update
    const startTime: Date = new Date();
    const updatedTableUsages: TableUsage [] = [];
    for (let i = 0; i < this.matchCards.length; i++) {
      const matchCard = this.matchCards[i];
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
    let tooltipText = '';
    for (let i = 0; i < this.matchesToPlayInfos.length; i++) {
      const matchInfo = this.matchesToPlayInfos[i];
      if (matchInfo.matchCard.id === matchCardId) {
        const eventName = matchInfo.tournamentEvent.name;
        const groupNumber = matchInfo.matchCard.groupNum;
        if (matchInfo.matchCard.drawType === 'ROUND_ROBIN') {
          tooltipText = `${eventName} R.R. Group ${groupNumber}`;
        } else {
          const roundName = this.getRoundShortName(matchInfo.matchCard.round);
          tooltipText = `${eventName} ${roundName} M ${groupNumber}`;
        }
      }
    }

    return tooltipText;
  }

  isSelectedMatchCard(matchCard: MatchCard) {
    return this.selectedMatchCardIds.includes(matchCard.id);
  }
}

export class MatchInfo {
  matchCard: MatchCard;
  tournamentEvent: TournamentEvent;
}
