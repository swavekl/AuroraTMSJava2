import {Component, Input, OnChanges, OnInit, SimpleChange, SimpleChanges} from '@angular/core';
import {PlayerMatchSummary} from '../model/player-match-summary';
import {TournamentEntry} from '../../tournament/tournament-entry/model/tournament-entry.model';
import {Router} from '@angular/router';

@Component({
  selector: 'app-player-results',
  templateUrl: './player-results.component.html',
  styleUrls: ['./player-results.component.scss']
})
export class PlayerResultsComponent implements OnInit, OnChanges {

  @Input()
  playerMatchSummaryList: PlayerMatchSummary[] = [];

  @Input()
  tournamentEntry: TournamentEntry;

  // summaries grouped by event
  summariesByEvent: any [];

  totalPointsExchanged: number;
  initialRating: number;
  newRating: number;
  matchesWon: number;
  matchesLost: number;

  constructor(private router: Router) {
    this.initialRating = 0;
    this.newRating = 0;
    this.totalPointsExchanged = 0;
    this.matchesLost = 0;
    this.matchesWon = 0;
  }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    const pmslChanges: SimpleChange = changes.playerMatchSummaryList;
    if (pmslChanges != null) {
      const playerMatchSummaryList = pmslChanges.currentValue;
      if (playerMatchSummaryList != null) {
        const tempSummariesByEventMap = new Map<string, PlayerMatchSummary[]>();
        playerMatchSummaryList.forEach((playerMatchSummary: PlayerMatchSummary) => {
          let eventSummaries: PlayerMatchSummary[] = tempSummariesByEventMap.get(playerMatchSummary.eventName);
          if (eventSummaries == null) {
            eventSummaries = [];
            tempSummariesByEventMap.set(playerMatchSummary.eventName, eventSummaries);
          }
          eventSummaries.push(playerMatchSummary);
        });

        const tempSummariesByEventList = [];
        tempSummariesByEventMap.forEach((eventSummaries, eventName) => {
          tempSummariesByEventList.push({eventName: eventName, eventSummaries: eventSummaries});
        });

        this.summariesByEvent = tempSummariesByEventList;

        this.totalPointsExchanged = 0;
        this.matchesWon = 0;
        this.matchesLost = 0;
        playerMatchSummaryList.forEach((playerMatchSummary: PlayerMatchSummary) => {
          this.totalPointsExchanged += playerMatchSummary.pointsExchanged;
          if (playerMatchSummary.matchWon) {
            this.matchesWon++;
          } else {
            this.matchesLost++;
          }
        });
      }
    }

    const teChange: SimpleChange = changes.tournamentEntry;
    if (teChange != null) {
      const tournamentEntry: TournamentEntry = teChange.currentValue;
      if (tournamentEntry != null) {
        this.initialRating = (tournamentEntry.eligibilityRating !== 0) ? tournamentEntry.seedRating : tournamentEntry.eligibilityRating;
      }
    }
    this.newRating = this.initialRating + this.totalPointsExchanged;
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

  back() {
    this.router.navigateByUrl('/ui/home');
  }
}
