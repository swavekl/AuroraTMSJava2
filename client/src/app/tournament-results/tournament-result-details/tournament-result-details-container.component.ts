import {Component, OnDestroy, OnInit} from '@angular/core';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {ActivatedRoute} from '@angular/router';
import {TournamentResultsService} from '../service/tournament-results.service';
import {combineLatest, Observable, of, Subscription} from 'rxjs';
import {TournamentEventConfigService} from '../../tournament/tournament-config/tournament-event-config.service';
import {createSelector} from '@ngrx/store';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {EventResults} from '../model/event-results';
import {DrawItem} from '../../draws/draws-common/model/draw-item.model';
import {MatchCardInfo} from '../../matches/model/match-card-info.model';
import {DrawService} from '../../draws/draws-common/service/draw.service';
import {MatchCardInfoService} from '../../matches/service/match-card-info.service';
import {DrawType} from '../../draws/draws-common/model/draw-type.enum';
import {first} from 'rxjs/operators';

@Component({
    selector: 'app-tournament-result-detail-container',
    template: `
    <app-tournament-result-details
      [selectedEvent]="event$ | async"
      [draws]="draws$ | async"
      [matchCardInfos]="matchCardInfos$ | async"
      [eventName]="eventName"
      [eventResultsList]="eventResultsList$ | async"
    [tournamentId]="tournamentId">
    </app-tournament-result-details>
  `,
    styles: [],
    standalone: false
})
export class TournamentResultDetailsContainerComponent implements OnInit, OnDestroy {

  // all event results for this event
  eventResultsList$: Observable<EventResults[]>;

  // event information
  event$: Observable<TournamentEvent>;

  eventName: string;

  tournamentId: number;

  public draws$: Observable<DrawItem[]>;

  // match card information i.e. without matches
  public matchCardInfos$: Observable<MatchCardInfo[]>;


  private subscriptions: Subscription = new Subscription();
  private loading$: Observable<boolean>;

  constructor(private linearProgressBarService: LinearProgressBarService,
              private activatedRoute: ActivatedRoute,
              private tournamentResultsService: TournamentResultsService,
              private tournamentEventConfigService: TournamentEventConfigService,
              private drawService: DrawService,
              private matchCardInfoService: MatchCardInfoService) {
    const strTournamentId = this.activatedRoute.snapshot.params['tournamentId'] || 0;
    this.tournamentId = Number(strTournamentId);
    const strEventId = this.activatedRoute.snapshot.params['eventId'] || 0;
    this.eventName = history?.state?.eventName;
    const eventId = Number(strEventId);
    this.setupProgressIndicator();
    this.loadTournamentEvent(this.tournamentId, eventId);
    this.loadEventResults(this.tournamentId, eventId);
    this.onLoadMatchCardInfos(eventId);
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  private setupProgressIndicator() {
    this.loading$ = combineLatest([
        this.tournamentEventConfigService.store.select(this.tournamentEventConfigService.selectors.selectLoading),
        this.tournamentResultsService.loading$,
        this.drawService.store.select(this.drawService.selectors.selectLoading),
        this.matchCardInfoService.loading$
      ],
      (eventConfigsLoading: boolean, resultsLoading: boolean, drawsLoading: boolean, matchCardInfosLoading: boolean) => {
        return eventConfigsLoading || resultsLoading || drawsLoading || matchCardInfosLoading;
      }
    );

    const loadingSubscription = this.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
  }

  private loadTournamentEvent(tournamentId: number, eventId: number) {
    const selector = createSelector(
      this.tournamentEventConfigService.selectors.selectEntityMap,
      (entityMap) => {
        return entityMap[eventId];
      });
    const subscription = this.tournamentEventConfigService.store.select(selector)
      .subscribe((tournamentEvent: TournamentEvent) => {
        if (!tournamentEvent) {
          this.tournamentEventConfigService.loadTournamentEvents(tournamentId).pipe(first()).subscribe();
        } else {
          this.event$ = of(tournamentEvent);
          this.loadDraws(tournamentEvent);
        }
      });
    this.subscriptions.add(subscription);
  }

  private loadEventResults(tournamentId: number, eventId: number) {
    this.eventResultsList$ = this.tournamentResultsService.getEventResults(tournamentId, eventId);
  }

  private onLoadMatchCardInfos(eventId: number) {
    this.matchCardInfos$ = this.matchCardInfoService.load(eventId);
  }

  private loadDraws(tournamentEvent: TournamentEvent) {
    const rounds = tournamentEvent.roundsConfiguration?.rounds || [];
    const lastRound = (rounds.length > 0) ? rounds[rounds.length - 1] : null;
    const isSingleElimination = (lastRound != null && lastRound.singleElimination);
    const drawType: DrawType = isSingleElimination ? DrawType.SINGLE_ELIMINATION : DrawType.ROUND_ROBIN;
    this.draws$ = this.drawService.loadForEvent(tournamentEvent.id, drawType);
  }
}
