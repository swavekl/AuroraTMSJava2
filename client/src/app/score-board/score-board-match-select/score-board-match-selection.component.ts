import {Component, Input, OnInit} from '@angular/core';
import {MatchCard} from '../../matches/model/match-card.model';
import {MonitorService} from '../../monitor/service/monitor.service';
import {MonitorMessage} from '../../monitor/model/monitor-message.model';
import {MonitorMessageType} from '../../monitor/model/monitor-message-type';
import {Router} from '@angular/router';

@Component({
  selector: 'app-score-board',
  templateUrl: './score-board-match-selection.component.html',
  styleUrls: ['./score-board-match-selection.component.scss']
})
export class ScoreBoardMatchSelectionComponent implements OnInit {

  @Input()
  matchCards: MatchCard [] = [];

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
}
