import {Component, Input, OnInit} from '@angular/core';
import {MatchCard} from '../../matches/model/match-card.model';
import {MonitorService} from '../../monitor/service/monitor.service';
import {MonitorMessage} from '../../monitor/model/monitor-message.model';
import {MonitorMessageType} from '../../monitor/model/monitor-message-type';
import {Router} from '@angular/router';
import {Match} from '../../matches/model/match.model';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {StartTimePipe} from '../../shared/pipes/start-time.pipe';

@Component({
  selector: 'app-score-board',
  templateUrl: './score-board-match-selection.component.html',
  styleUrls: ['./score-board-match-selection.component.scss']
})
export class ScoreBoardMatchSelectionComponent implements OnInit {

  @Input()
  matchCards: MatchCard [] = [];

  @Input()
  tournamentEvents: TournamentEvent[] = [];

  @Input()
  tournamentId: number;

  @Input()
  tableNumber: number;

  @Input()
  tournamentDay: number;

  constructor(private monitorService: MonitorService,
              private router: Router) { }

  ngOnInit(): void {
  }

  getPlayerNames(matchCard: MatchCard, letter: string) {
    if (this.matchCards != null && matchCard.profileIdToNameMap != null) {
      const match = matchCard?.matches[0];
      const profileId = (letter === 'A') ? match.playerAProfileId : match.playerBProfileId;
      return matchCard.profileIdToNameMap[profileId];
    } else {
      return `player ${letter}`;
    }
  }

  areMatchPlayersDetermined(matchCard: MatchCard): boolean {
    if (this.matchCards != null && matchCard.profileIdToNameMap != null) {
      const match = matchCard?.matches[0];
      return match.playerAProfileId != Match.TBD_PROFILE_ID && match.playerBProfileId != Match.TBD_PROFILE_ID;
    } else {
      return false;
    }
  }

  getEventName(matchCard: MatchCard) {
    if (this.tournamentEvents != null && this.tournamentEvents.length > 0) {
      const events: TournamentEvent[] = this.tournamentEvents.filter(
        (tournamentEvent: TournamentEvent) => {
        return tournamentEvent.id === matchCard.eventFk;
      });
      if (events?.length > 0) {
        return events[0].name;
      }
    } else {
      return '';
    }
  }

  selectedMatch(matchCard: MatchCard) {
    const matchIndex = 0;
    const monitorMessage: MonitorMessage = {
      messageType: MonitorMessageType.ScoreUpdate,
      match: matchCard.matches[matchIndex],
      playerAName: this.getPlayerNames(matchCard, 'A'),
      playerBName: this.getPlayerNames(matchCard, 'B'),
      playerAPartnerName: 'X',
      playerBPartnerName: 'Y',
      isDoubles: true,
      numberOfGames: 5,
      timeoutStarted: false,
      timeoutRequester: null,
      warmupStarted: false
    };
    this.monitorService.sendMessage(this.tournamentId, this.tableNumber, monitorMessage);

    const matchCardId = matchCard.id;
    const url = `/ui/scoreboard/scoreentry/${this.tournamentId}/${this.tournamentDay}/${this.tableNumber}/${matchCardId}/${matchIndex}`;
    this.router.navigateByUrl(url);
  }

  back() {
    const url = `/ui/scoreboard`
    this.router.navigateByUrl(url);
  }
}
