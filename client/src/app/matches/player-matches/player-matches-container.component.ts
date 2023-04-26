import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {MatchCardService} from '../service/match-card.service';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {combineLatest, Observable, Subscription} from 'rxjs';
import {createSelector} from '@ngrx/store';
import {MatchCard} from '../model/match-card.model';
import {TournamentEventConfigService} from '../../tournament/tournament-config/tournament-event-config.service';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {first} from 'rxjs/operators';
import {MatchService} from '../service/match.service';
import {Match} from '../model/match.model';

/**
 * List of matches for a single match card for viewing by player
 */
@Component({
  selector: 'app-player-matches-container',
  template: `
    <app-player-matches
      [matchCard]="matchCard$ | async"
      [tournamentId]="tournamentId"
      [doubles]="doubles"
      [pointsPerGame]="pointsPerGame"
      [expandedMatchIndex]="expandedMatchIndex"
      (updateMatch)="onUpdateMatch($event)"
      (back)="onGoBack()"
      (enterMatchScore)="onEnterScore($event)">
    </app-player-matches>
  `,
  styles: []
})
export class PlayerMatchesContainerComponent implements OnInit, OnDestroy {

  public matchCard$: Observable<MatchCard>;

  private loading$: Observable<boolean>;

  private matchCardId: number;

  public expandedMatchIndex: number;

  public doubles: boolean;

  public pointsPerGame: number;

  public tournamentId: number;
  public tournamentEntryId: number;
  public tournamentDay: number;

  private returnUrl: string;

  private tournamentEvent$: Observable<TournamentEvent>;

  private subscriptions: Subscription = new Subscription();

  constructor(private activatedRoute: ActivatedRoute,
              private linearProgressBarService: LinearProgressBarService,
              private matchCardService: MatchCardService,
              private matchService: MatchService,
              private tournamentEventConfigService: TournamentEventConfigService,
              private router: Router) {
    this.tournamentId = this.activatedRoute.snapshot.params['tournamentId'] || 0;
    this.tournamentDay = this.activatedRoute.snapshot.params['tournamentDay'] || 1;
    this.tournamentEntryId = this.activatedRoute.snapshot.params['tournamentEntryId'] || 0;
    this.matchCardId = this.activatedRoute.snapshot.params['matchCardId'] || 0;
    this.doubles = (history?.state?.doubles === true);
    this.expandedMatchIndex = isNaN(history?.state?.matchIndex) ? 0 : parseInt(history.state.matchIndex, 10);
    this.returnUrl = `/ui/today/playerscheduledetail/${this.tournamentId}/${this.tournamentDay}/${this.tournamentEntryId}/${this.matchCardId}`;
    this.pointsPerGame = 11;
    this.setupProgressIndicator();
    this.loadMatchesInformation(this.matchCardId);
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  private setupProgressIndicator() {
    this.loading$ = combineLatest(
      this.tournamentEventConfigService.store.select(this.tournamentEventConfigService.selectors.selectLoading),
      this.matchCardService.store.select(this.matchCardService.selectors.selectLoading),
      (eventConfigLoading: boolean, matchCardLoading: boolean) => {
        return eventConfigLoading || matchCardLoading;
      }
    );

    const loadingSubscription = this.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
  }

  /**
   *
   * @param matchCardId
   * @private
   */
  private loadMatchesInformation(matchCardId: number) {
    this.matchCardService.store.select(this.matchCardService.selectors.selectLoading);
    const selectedEntrySelector = createSelector(
      this.matchCardService.selectors.selectEntityMap,
      (entityMap) => {
        return entityMap[matchCardId];
      });
    this.matchCard$ = this.matchCardService.store.select(selectedEntrySelector);
    const subscription = this.matchCard$.subscribe((matchCard: MatchCard) => {
      if (matchCard == null) {
        // console.log('requesting match card from server');
        // get from the server if not cached yet
        this.matchCardService.getByKey(matchCardId);
      } else {
        // console.log('got match card');
        this.loadEventInformation(this.tournamentId, matchCard.eventFk);
      }
    });
    this.subscriptions.add(subscription);
  }

  /**
   *
   * @param tournamentId
   * @param eventId
   * @private
   */
  private loadEventInformation(tournamentId: number, eventId: number) {
    this.tournamentEventConfigService.store.select(this.tournamentEventConfigService.selectors.selectLoading);
    const selectedEntrySelector = createSelector(
      this.tournamentEventConfigService.selectors.selectEntityMap,
      (tournamentEvents) => {
        return tournamentEvents[eventId];
      });
    this.tournamentEvent$ = this.tournamentEventConfigService.store.select(selectedEntrySelector);
    const subscription = this.tournamentEvent$.subscribe((tournamentEvent: TournamentEvent) => {
      if (tournamentEvent == null) {
        // console.log('loading events from server for tournament ' + tournamentId);
        // get from the server if not cached yet
        this.tournamentEventConfigService.loadTournamentEvents(tournamentId).pipe(first()).subscribe();
      } else {
        // console.log('got event from cache');
        this.pointsPerGame = tournamentEvent.pointsPerGame;
        this.tournamentId = tournamentEvent.tournamentFk;
        this.doubles = tournamentEvent.doubles;
      }
    });
    this.subscriptions.add(subscription);
  }

  onGoBack() {
    const extras = {
      state: {
        doubles: this.doubles
      }
    };
    this.router.navigateByUrl(this.returnUrl, extras);
  }

  onEnterScore(event) {
    const url = `/ui/matches/scoreentryphone/${this.tournamentId}/${this.tournamentDay}/${this.tournamentEntryId}/${this.matchCardId}/${event.matchIndex}`;
    const extras = {
      state: {
        doubles: this.doubles
      }
    };
    this.router.navigateByUrl(url, extras);
  }

  public onUpdateMatch(updatedMatch: Match) {
    const subscription = this.matchService.update(updatedMatch)
      .pipe(first())
      .subscribe((match: Match) => {
        // console.log('Finished updating match', match);
        this.matchCardService.getByKey(this.matchCardId);
    });
    this.subscriptions.add(subscription);
  }
}
