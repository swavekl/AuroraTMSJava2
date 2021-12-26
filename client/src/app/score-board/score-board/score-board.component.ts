import {Component, Input, OnInit} from '@angular/core';
import {MatchCard} from '../../matches/model/match-card.model';
import {MonitorService} from '../../monitor/service/monitor.service';
import {MonitorMessage} from '../../monitor/model/monitor-message.model';
import {MonitorMessageType} from '../../monitor/model/monitor-message-type';

@Component({
  selector: 'app-score-board',
  templateUrl: './score-board.component.html',
  styleUrls: ['./score-board.component.css']
})
export class ScoreBoardComponent implements OnInit {

  @Input()
  matchCards: MatchCard [] = [];

  @Input()
  tournamentId: number;

  @Input()
  tableNumber: number;

  constructor(private monitorService: MonitorService) { }

  ngOnInit(): void {
    console.log('match cards', this.matchCards);
  }

  getPlayerNames(matchCard: MatchCard, letter: string) {
    if (this.matchCards != null && matchCard.profileIdToNameMap != null) {
      const match = matchCard?.matches[0];
      const profileId = (letter === 'A') ? match.playerAProfileId : match.playerBProfileId;
      const playerName = matchCard.profileIdToNameMap[profileId];
      console.log('playerName', playerName);
      return playerName;
    } else {
      return letter;
    }
  }

  selectedMatch(matchCard: MatchCard) {
    const monitorMessage: MonitorMessage = {
      messageType: MonitorMessageType.ScoreUpdate,
      match: matchCard.matches[0],
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

  }
}
