import {Component, Input, OnChanges, OnInit, SimpleChange, SimpleChanges} from '@angular/core';
import {EventResults} from '../model/event-results';
import {DrawItem} from '../../draws/draws-common/model/draw-item.model';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {MatchCardInfo} from '../../matches/model/match-card-info.model';

@Component({
    selector: 'app-tournament-result-details',
    templateUrl: './tournament-result-details.component.html',
    styleUrls: ['./tournament-result-details.component.scss'],
    standalone: false
})
export class TournamentResultDetailsComponent implements OnInit, OnChanges {

  @Input()
  eventResultsList: EventResults[];

  @Input()
  selectedEvent: TournamentEvent;

  @Input()
  eventName: string;

  @Input()
  tournamentId: number;

  @Input() draws!: DrawItem[];

  @Input() matchCardInfos!: MatchCardInfo[];

  // more efficient than using *ngIf to show/hide tabs
  showTabs = false;

  hasRRRound: boolean = false;
  hasSERound: boolean = false;

  roundNumbers: number[] = [];

  constructor() {
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    const eventChanges: SimpleChange = changes.selectedEvent;
    if (eventChanges != null && eventChanges.currentValue != null) {
      const selectedEvent: TournamentEvent = eventChanges.currentValue;
      const rounds = selectedEvent.roundsConfiguration?.rounds || [];
      const len = rounds.length ?? 0;
      this.showTabs = len > 1;
      if (rounds && rounds.length > 0) {
        let hasRRRound = false;
        let hasSERound = false;
        for (const round of rounds) {
          hasRRRound = !round.singleElimination || hasRRRound;
          hasSERound = round.singleElimination || hasSERound;
        }
        this.hasRRRound = hasRRRound;
        this.hasSERound = hasSERound;
      }

      const roundNumbers: number [] = [];
      for (let i = 0; i < rounds.length; i++) {
        const round = rounds[i];
        roundNumbers.push(i);
      }
      this.roundNumbers = roundNumbers;
    }
  }

  public getBracketsHeight(): string {
    return (window.innerHeight - 232) + 'px';
  }

  getDoublesPlayerName(playerNames: string, index: number): string {
    const playerNamesArray: string [] = playerNames.split('/');
    if (playerNamesArray.length === 2) {
      const playerName = playerNamesArray[index].trim();
      return (index === 0) ? playerName + ' /' : playerName;
    } else {
      return playerNames;
    }
  }

  trackByRound(_index: number, round: any): number {
    return round.ordinalNum; // stable per event (1, 2, 3...)
  }
}
