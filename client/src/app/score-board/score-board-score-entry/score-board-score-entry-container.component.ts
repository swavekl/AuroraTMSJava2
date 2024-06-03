import {Component, OnDestroy, OnInit} from '@angular/core';
import {Observable, of, Subscription} from 'rxjs';
import {Match} from '../../matches/model/match.model';
import {ActivatedRoute, Router} from '@angular/router';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {MatchCardService} from '../../matches/service/match-card.service';
import {MatchService} from '../../matches/service/match.service';
import {createSelector} from '@ngrx/store';
import {MatchCard} from '../../matches/model/match-card.model';
import {first, switchMap, tap} from 'rxjs/operators';
import {MonitorService} from '../../monitor/service/monitor.service';
import {MonitorMessage} from '../../monitor/model/monitor-message.model';
import {MonitorMessageType} from '../../monitor/model/monitor-message-type';

@Component({
  selector: 'app-score-board-score-entry-container',
  template: `
    <app-score-board-score-entry
      [match]="match$ | async"
      [playerAName]="playerAName$ | async"
      [playerBName]="playerBName$ | async"
      [numberOfGames]="numberOfGames"
      [pointsPerGame]="pointsPerGame"
      [doubles]="doubles"
      (saveMatch)="onSaveMatch($event)"
      (cancelMatch)="onCancelMatch()">
    </app-score-board-score-entry>
  `,
  styles: ``
})
export class ScoreBoardScoreEntryContainerComponent implements OnDestroy {
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
    this.matchCardService.store.select(this.matchCardService.selectors.selectLoading);
    const selectedEntrySelector = createSelector(
      this.matchCardService.selectors.selectEntityMap,
      (entityMap) => {
        return entityMap[matchCardId];
      });
    const matchCard$: Observable<MatchCard> = this.matchCardService.store.select(selectedEntrySelector);
    const subscription = matchCard$.subscribe((matchCard: MatchCard) => {
      if (matchCard == null) {
        // console.log('getting match card from server');
        // get from the server if not cached yet
        this.matchCardService.getByKey(matchCardId);
      } else {
        // console.log('got match card from cache', matchCard);
        const allMatches = matchCard.matches;
        if (this.matchIndex < allMatches.length) {
          const match = allMatches[matchIndex];
          // console.log('cloning match');
          const cloneOfMatch: Match = JSON.parse(JSON.stringify(match));
          this.match$ = of(cloneOfMatch);
          this.saveMatchState(match);
          // console.log('match is', match);
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
  onSaveMatch(event: any) {
    const updatedMatch: Match = event.updatedMatch;
    this.matchService.update(updatedMatch)
      .pipe(first(),
        tap({
          next: (match: Match) => {
            this.sendMonitorUpdate(updatedMatch);
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

  private sendMonitorUpdate(match: Match) {
    let playerAName: string = this.playerAName;
    let playerBName: string = this.playerBName;
    let playerAPartnerName = 'X';
    let playerBPartnerName = 'Y';
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
      messageType: MonitorMessageType.ScoreUpdate,
      match: match,
      playerAName: playerAName,
      playerBName: playerBName,
      playerAPartnerName: playerAPartnerName,
      playerBPartnerName: playerBPartnerName,
      doubles: this.doubles,
      pointsPerGame: this.pointsPerGame,
      numberOfGames: this.numberOfGames,
      timeoutStarted: this.isTimeoutStarted(match),
      timeoutRequester: this.getTimeoutRequester(match),
      warmupStarted: false
    };
    this.monitorService.sendMessage(this.tournamentId, this.tableNumber, monitorMessage);

  }

  private isTimeoutStarted(match: Match) {
    return (!this.savedMatch.sideATimeoutTaken && match.sideATimeoutTaken) ||
           (!this.savedMatch.sideBTimeoutTaken && match.sideBTimeoutTaken)
  }

  private getTimeoutRequester(match: Match): string {
    let taken : string = null;
    if (!this.savedMatch.sideATimeoutTaken && match.sideATimeoutTaken) {
      taken = this.playerAName;
    } else if (!this.savedMatch.sideBTimeoutTaken && match.sideBTimeoutTaken) {
      taken = this.playerBName;
    }
    return taken;
  }
}
