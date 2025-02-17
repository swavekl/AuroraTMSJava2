import {Component, OnDestroy} from '@angular/core';
import {Observable, of, Subscription} from 'rxjs';
import {ActivatedRoute, Router} from '@angular/router';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {MatchCardService} from '../../matches/service/match-card.service';
import {MatchService} from '../../matches/service/match.service';
import {MonitorService} from '../../monitor/service/monitor.service';
import {createSelector} from '@ngrx/store';
import {MatchCard} from '../../matches/model/match-card.model';
import {Match} from '../../matches/model/match.model';
import {first, tap} from 'rxjs/operators';
import {MonitorMessageType} from '../../monitor/model/monitor-message-type';
import {MonitorMessage} from '../../monitor/model/monitor-message.model';

@Component({
  selector: 'app-score-board-match-start-container',
  template: `
    <app-score-board-match-start
      [match]="match$ | async"
      [playerAName]="playerAName$ | async"
      [playerBName]="playerBName$ | async"
      [numberOfGames]="numberOfGames"
      [pointsPerGame]="pointsPerGame"
      [doubles]="doubles"
      (timerEvent)="onTimerEvent($event)"
      (matchEvent)="onMatchEvent($event)"
      (saveMatch)="onServerReceiverEvent($event)"
      (serverReceiverSaveEvent)="onServerReceiverEvent($event)"
    >
      score-board-match-start-container works!
    </app-score-board-match-start>
  `,
  styles: ``
})
export class ScoreBoardMatchStartContainerComponent implements OnDestroy {

  public match$: Observable<Match>;
  public savedMatch: Match;

  public playerAName$: Observable<String>;
  public playerBName$: Observable<String>;
  public playerAName: string;
  public playerBName: string;

  private tournamentId: number;
  private tableNumber: number;
  private matchCardId: number;
  private matchIndex: number;
  public numberOfGames: number;
  public pointsPerGame: number;
  private returnUrl: string;
  private startMatchUrl: string;

  public doubles: boolean;

  private subscriptions: Subscription = new Subscription();

  constructor(private activatedRoute: ActivatedRoute,
              private linearProgressBarService: LinearProgressBarService,
              private matchCardService: MatchCardService,
              private matchService: MatchService,
              private router: Router,
              private monitorService: MonitorService) {
    this.tournamentId = this.activatedRoute.snapshot.params['tournamentId'] || 0;
    const tournamentDay = this.activatedRoute.snapshot.params['tournamentDay'] || 1;
    this.tableNumber = this.activatedRoute.snapshot.params['tableNumber'] || 0;
    this.matchCardId = this.activatedRoute.snapshot.params['matchCardId'] || 0;
    this.matchIndex = this.activatedRoute.snapshot.params['matchIndex'] || 0;
    this.doubles = (history?.state?.doubles === true);
    this.returnUrl = `/ui/scoreboard/selectmatch/${this.tournamentId}/${tournamentDay}/${this.tableNumber}`;
    this.startMatchUrl = `/ui/scoreboard/scoreentry/${this.tournamentId}/${tournamentDay}/${this.tableNumber}/${this.matchCardId}/${this.matchIndex}`;
    this.pointsPerGame = 11;
    this.numberOfGames = 5;
    this.setupProgressIndicator();
    this.loadMatchInformation(this.matchCardId, this.matchIndex);
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  private setupProgressIndicator() {
    const subscription = this.matchCardService.store.select(this.matchCardService.selectors.selectLoading)
      .subscribe((loading: boolean) => {
        this.linearProgressBarService.setLoading(loading);
      });
    this.subscriptions.add(subscription);
  }

  /**
   *
   * @param matchCardId
   * @param matchIndex
   * @private
   */
  private loadMatchInformation(matchCardId: number, matchIndex: number) {
    this.matchCardService.store.select(this.matchCardService.selectors.selectLoading)
    const selectedEntrySelector = createSelector(
      this.matchCardService.selectors.selectEntityMap,
      (entityMap) => {
        return entityMap[matchCardId];
      });
    const matchCard$: Observable<MatchCard> = this.matchCardService.store.select(selectedEntrySelector);
    const subscription = matchCard$.subscribe((matchCard: MatchCard) => {
      if (matchCard == null) {
        // get from the server if not cached yet
        this.matchCardService.getByKey(matchCardId);
      } else {
        const allMatches = matchCard.matches;
        if (this.matchIndex < allMatches.length) {
          const match = allMatches[matchIndex];
          console.log('match umpired ', match.matchUmpired);
          const cloneOfMatch: Match = JSON.parse(JSON.stringify(match));
          this.match$ = of(cloneOfMatch);
          this.saveMatchState(match);
          // console.log('match is', match);
          // console.log('matchCard.profileIdToNameMap', matchCard.profileIdToNameMap);
          this.playerAName$ = of(matchCard.profileIdToNameMap[match.playerAProfileId]);
          this.playerBName$ = of(matchCard.profileIdToNameMap[match.playerBProfileId]);
          this.playerAName = matchCard.profileIdToNameMap[match.playerAProfileId];
          this.playerBName = matchCard.profileIdToNameMap[match.playerBProfileId];
        }
        this.numberOfGames = matchCard.numberOfGames;
      }
    });
    this.subscriptions.add(subscription);
  }

  /**
   *
   * @param event
   */
  onServerReceiverEvent(event: any) {
    const updatedMatch: Match = event.updatedMatch;
    this.matchService.update(updatedMatch)
      .pipe(first(),
        tap({
          next: () => {
            this.saveMatchState(updatedMatch);
            this.matchCardService.clearCache();
            // this.sendMonitorUpdate(updatedMatch);
          }
        })
      ).subscribe();
  }

  onCancelMatch() {
    this.router.navigateByUrl(this.returnUrl);
  }

  private saveMatchState(match: Match) {
    const cloneOfMatch: Match = JSON.parse(JSON.stringify(match));
    this.savedMatch = cloneOfMatch;
  }

  onTimerEvent(event: any) {
    const updatedMatch: Match = event.updatedMatch;
    const action: string = event.action;
    this.sendMonitorUpdate(updatedMatch, action);
    this.matchService.update(updatedMatch)
      .pipe(first(),
        tap({
          next: () => {
            this.saveMatchState(updatedMatch);
          }
        })
      ).subscribe();
  }

  private sendMonitorUpdate(match: Match, action: string) {
    let playerAName: string = this.playerAName;
    let playerBName: string = this.playerBName;
    let playerAPartnerName = '';
    let playerBPartnerName = '';
    if (this.doubles) {
      const teamAPlayerNames = this.playerAName.split('/');
      if (teamAPlayerNames.length === 2) {
        playerAName = teamAPlayerNames[0];
        playerAPartnerName = teamAPlayerNames[1];
      }
      const teamBPlayerNames = playerBName.split('/');
      if (teamBPlayerNames.length === 2) {
        playerBName = teamBPlayerNames[0];
        playerBPartnerName = teamBPlayerNames[1];
      }
    }
    const monitorMessage: MonitorMessage = {
      match: match,
      playerAName: playerAName,
      playerBName: playerBName,
      playerAPartnerName: playerAPartnerName,
      playerBPartnerName: playerBPartnerName,
      doubles: this.doubles,
      pointsPerGame: this.pointsPerGame,
      numberOfGames: this.numberOfGames,
    };
    this.monitorService.sendMessage(this.tournamentId, this.tableNumber, monitorMessage);
  }

  onMatchEvent($event: any) {
    const action = $event.action;
    switch (action) {
      case 'back':
        this.router.navigateByUrl(this.returnUrl);
        break;
      case 'startMatch':
        this.router.navigateByUrl(this.startMatchUrl);
        break;
    }
  }
}
