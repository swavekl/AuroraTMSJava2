import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {TableUsage} from '../model/table-usage.model';
import {MatchCard} from '../../matches/model/match-card.model';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {DrawType} from '../../draws/model/draw-type.enum';
import {Match} from '../../matches/model/match.model';

@Component({
  selector: 'app-table-usage',
  templateUrl: './table-usage.component.html',
  styleUrls: ['./table-usage.component.scss']
})
export class TableUsageComponent implements OnInit, OnChanges {

  @Input()
  public tableUsageList: TableUsage [] = [];

  @Input()
  matchCards: MatchCard[];

  @Input()
  tournamentEvents: TournamentEvent[];

  public matchesToPlayInfos: MatchInfo [];

  public selectedEventId: number;

  constructor() {
    this.selectedEventId = 0;
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
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
    console.log('starting matches for event ', this.selectedEventId);
  }

  onPrintEventMatchCards() {
    console.log('printing event match cards', this.selectedEventId);
  }
}

export class MatchInfo {
  matchCard: MatchCard;
  tournamentEvent: TournamentEvent;
}
