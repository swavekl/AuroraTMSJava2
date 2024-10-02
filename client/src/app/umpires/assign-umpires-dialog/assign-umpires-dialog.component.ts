import {Component, Inject} from '@angular/core';
import {MatSelectChange} from '@angular/material/select';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';

import {combineLatest, forkJoin, Observable} from 'rxjs';
import {first, map} from 'rxjs/operators';

import {UmpiringService} from '../service/umpiring.service';
import {TournamentEventConfigService} from '../../tournament/tournament-config/tournament-event-config.service';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {MatchCardService} from '../../matches/service/match-card.service';
import {MatchCard} from '../../matches/model/match-card.model';
import {UmpireWork} from '../model/umpire-work.model';
import {Match} from '../../matches/model/match.model';
import {Personnel} from '../../tournament/tournament-config/model/personnel.model';

@Component({
  selector: 'app-assign-umpires-dialog',
  templateUrl: './assign-umpires-dialog.component.html',
  styleUrl: './assign-umpires-dialog.component.scss'
})
export class AssignUmpiresDialogComponent {

  tournamentId: number;
  eventId: number;
  matchId: number;
  tournamentEvents$: Observable<TournamentEvent[]>;
  tournamentEvents: TournamentEvent[] = [];

  matchCards$: Observable<MatchCard[]>;
  private tournamentDay: number = 0;

  // event id to all match cards associative array
  allEventsToMatchInfosMap: MatchInfo [][] = [];

  // names of all match cards for one event - currently selected
  oneEventMatchInfos: MatchInfo [];

  // selected match player names
  playerNames: string;

  // list of available umpire in this tournament
  umpireList: Personnel[];

  // selected umpires profile id
  umpireProfileId: string;

  // optional assistant umpire
  assistantUmpireProfileId: string;

  constructor(public dialogRef: MatDialogRef<AssignUmpiresDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private umpiringService: UmpiringService,
              private tournamentEventConfigService: TournamentEventConfigService,
              private matchCardService: MatchCardService) {
    this.tournamentId = data?.tournamentId;
    this.tournamentDay = data?.tournamentDay ?? 0;
    this.umpireList = data?.umpireList;
    this.loadTournamentEvents(this.tournamentId);
    this.setupMatchCards(this.tournamentDay);
    this.getMatchCardsWithNames();
  }

  private loadTournamentEvents(tournamentId: number) {
    this.tournamentEvents$ = this.tournamentEventConfigService.store.select(
      this.tournamentEventConfigService.selectors.selectEntities);
    // load them - they will surface via this selector
    this.tournamentEventConfigService.loadTournamentEvents(tournamentId)
      .pipe(first()).subscribe();
  }

  private setupMatchCards(tournamentId: number) {
    this.matchCards$ = this.matchCardService.store.select(this.matchCardService.selectors.selectEntities);
    this.loadMatchCardsForDay(this.tournamentDay);
  }

  private loadMatchCardsForDay(day: number) {
    this.matchCardService.loadAllForTheTournamentDay(this.tournamentId, day, true)
      .pipe(first()).subscribe();
  }

  onCancel() {
    this.dialogRef.close({action: 'cancel', umpireWork: null});
  }

  onAssign() {
    const work: UmpireWork = {
      id: null,
      tournamentFk: this.tournamentId,
      eventFk: this.eventId,
      matchFk: this.matchId,
      matchDate: new Date(),
      assistantUmpireProfileId: this.assistantUmpireProfileId,
      umpireProfileId: this.umpireProfileId
    };
    this.dialogRef.close({action: 'assign', umpireWork: work});
  }

  onChangeEvent($event: MatSelectChange) {
    const eventId: number = $event.value;
    this.filterOneEventMatchCards(eventId);
  }

  private filterOneEventMatchCards(eventId: number) {
    this.oneEventMatchInfos = this.allEventsToMatchInfosMap[eventId];
  }

  private getEventName(eventId: number, tournamentEvents: TournamentEvent[]): string {
    const tournamentEvent = tournamentEvents.find((tournamentEvent: TournamentEvent, index: number, events: TournamentEvent[]) => {
      return (tournamentEvent.id === eventId);
    });
    return (tournamentEvent != null) ? tournamentEvent.name : '';
  }

  private getMatchCardsWithNames() {
    combineLatest([this.tournamentEvents$, this.matchCards$])
      .pipe(
        map(([tournamentEvents, matchCards]: [TournamentEvent[], MatchCard[]]) => {
            if (tournamentEvents?.length > 0 && matchCards?.length > 0 && this.tournamentDay != 0) {
              // get only events for today
              this.tournamentEvents = tournamentEvents.filter((tournamentEvent: TournamentEvent) => {
                return tournamentEvent.day === this.tournamentDay;
              });
              // create match infos for all matches for all events
              for (const matchCard of matchCards) {
                const eventName = this.getEventName(matchCard.eventFk, this.tournamentEvents);
                const matchIdentifierText = MatchCard.getFullMatchName(eventName, matchCard.drawType, matchCard.round, matchCard.groupNum);
                let matchInfosForEvent: MatchInfo[] = this.allEventsToMatchInfosMap[matchCard.eventFk];
                if (matchInfosForEvent == undefined) {
                  matchInfosForEvent = [];
                  this.allEventsToMatchInfosMap[matchCard.eventFk] = matchInfosForEvent;
                }
                matchCard.matches.forEach((match: Match) => {
                  const playerAName = matchCard.profileIdToNameMap[match.playerAProfileId];
                  const playerBName = matchCard.profileIdToNameMap[match.playerBProfileId];
                  const tooltip: string = `${playerAName} vs ${playerBName}`;
                  const description = `${matchIdentifierText} M${match.matchNum}`;
                  const matchInfo: MatchInfo = {id: match.id, description: description, matchPlayers: tooltip};
                  matchInfosForEvent.push(matchInfo);
                });
              }
              // show only first event match cards
              if (tournamentEvents.length > 0) {
                this.filterOneEventMatchCards(tournamentEvents[0].id);
              }
            }
          }
        )).subscribe();
  }

  onChangeMatch($event: MatSelectChange) {
    const matchId = $event.value;
    const matchInfo: MatchInfo = this.oneEventMatchInfos.find((matchInfo: any) => {
      return matchInfo.id === matchId;
    });
    this.playerNames = matchInfo?.matchPlayers;
  }

  onChangeUmpire($event: MatSelectChange) {
    this.umpireProfileId = $event.value;
  }

  onChangeAssistantUmpire($event: MatSelectChange) {
    this.assistantUmpireProfileId = $event.value;
  }
}

export interface MatchInfo {
  id: number;
  description: string;
  matchPlayers: string;
}
