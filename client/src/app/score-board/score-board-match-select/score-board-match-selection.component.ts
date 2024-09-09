import {Component, Input, OnInit} from '@angular/core';
import {Router} from '@angular/router';

import {MatchCard} from '../../matches/model/match-card.model';
import {MonitorService} from '../../monitor/service/monitor.service';
import {MonitorMessage} from '../../monitor/model/monitor-message.model';
import {MonitorMessageType} from '../../monitor/model/monitor-message-type';
import {Match} from '../../matches/model/match.model';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {MatchService} from '../../matches/service/match.service';
import {first} from 'rxjs/operators';

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
              private router: Router,
              private matchService: MatchService) { }

  ngOnInit(): void {
  }

  getPlayerNames(matchCard: MatchCard, matchIndex: number, playerSide: string) {
    if (this.matchCards != null && matchCard.profileIdToNameMap != null) {
      const match = matchCard?.matches[matchIndex];
      const profileId = (playerSide === 'A') ? match.playerAProfileId : match.playerBProfileId;
      return matchCard.profileIdToNameMap[profileId];
    } else {
      return `player ${playerSide}`;
    }
  }

  areMatchPlayersDetermined(matchCard: MatchCard, matchIndex: number): boolean {
    if (this.matchCards != null && matchCard.profileIdToNameMap != null) {
      const match = matchCard?.matches[matchIndex];
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

  selectedMatch(matchCard: MatchCard, matchIndex: number) {
    let currentMatch: Match = matchCard.matches[matchIndex];
    const tournamentEvent : TournamentEvent [] = this.tournamentEvents.filter((event:TournamentEvent) => { return event.id === matchCard.eventFk; });
    const numberOfGames: number = matchCard.numberOfGames;
    const doubles: boolean =    (tournamentEvent?.length > 0) ? tournamentEvent[0].doubles : false;
    // const messageType: MonitorMessageType = (currentMatch?.messageType === null)
    //   ? MonitorMessageType.StartMatch : currentMatch.messageType;
    currentMatch = {
      ...currentMatch,
      matchUmpired: true,
      messageType: MonitorMessageType.StartMatch
    };

    // if (!currentMatch.matchUmpired) {
      // starting a match
      this.matchService.update(currentMatch)
        .pipe(first())
        .subscribe(
          {
            next: updatedMatch => {
              this.navigateToMatchScoreEntry(matchCard, matchIndex, doubles);
            },
            error: err => {
              console.log('Error updating match', err);
            }
          }
        );
    // } else {
    //   const pointsPerGame: number = (tournamentEvent?.length > 0) ? tournamentEvent[0].pointsPerGame : 11;
    //   let playerAName: string = this.getPlayerNames(matchCard, matchIndex, 'A');
    //   let playerBName: string = this.getPlayerNames(matchCard, matchIndex, 'B');
    //   let playerAPartnerName = 'X';
    //   let playerBPartnerName = 'Y';
    //   if (doubles) {
    //      const teamAPlayerNames = playerAName.split('/');
    //      if (teamAPlayerNames.length === 2) {
    //        playerAName = teamAPlayerNames[0];
    //        playerAPartnerName = teamAPlayerNames[1];
    //      }
    //      const teamBPlayerNames = playerBName.split('/');
    //      if (teamBPlayerNames.length === 2) {
    //        playerBName = teamBPlayerNames[0];
    //        playerBPartnerName = teamBPlayerNames[1];
    //      }
    //   }
    //   const monitorMessage: MonitorMessage = {
    //     match: currentMatch,
    //     playerAName: playerAName,
    //     playerBName: playerBName,
    //     playerAPartnerName: playerAPartnerName,
    //     playerBPartnerName: playerBPartnerName,
    //     doubles: doubles,
    //     numberOfGames: numberOfGames,
    //     pointsPerGame: pointsPerGame,
    //   };
    //   this.monitorService.sendMessage(this.tournamentId, this.tableNumber, monitorMessage);
    //   this.navigateToMatchScoreEntry(matchCard, matchIndex, doubles);
    // }
  }

  /**
   *
   * @param matchCard
   * @param matchIndex
   * @param doubles
   * @private
   */
  private navigateToMatchScoreEntry(matchCard: MatchCard, matchIndex: number, doubles: boolean) {
    const matchCardId = matchCard.id;
    const url = `/ui/scoreboard/matchstart/${this.tournamentId}/${this.tournamentDay}/${this.tableNumber}/${matchCardId}/${matchIndex}`;
    const extras = {
      state: {
        doubles: doubles
      }
    };
    this.router.navigateByUrl(url, extras);
  }

  back() {
    const url = `/ui/scoreboard`
    this.router.navigateByUrl(url);
  }
}
