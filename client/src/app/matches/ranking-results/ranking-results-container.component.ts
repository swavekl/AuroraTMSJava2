import {AfterViewInit, Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {combineLatest, Observable, of, Subscription} from 'rxjs';
import {createSelector} from '@ngrx/store';

import {MatchCardService} from '../service/match-card.service';
import {MatchCard} from '../model/match-card.model';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {RankingResultsComponent} from './ranking-results.component';
import {TournamentEventConfigService} from '../../tournament/tournament-config/tournament-event-config.service';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {first, tap} from 'rxjs/operators';

@Component({
    selector: 'app-ranking-results-container',
    template: `
    <mat-toolbar>
      <button type="button" mat-raised-button [routerLink]="returnUrl">Back to Schedule</button>
    </mat-toolbar>
    <div class="mat-subtitle-1" style="margin-left: 10px;">
      <div>{{eventName}}</div>
      <div>
        <span>{{ round | roundName: group }}</span>
        <span *ngIf="round === 0">, Group: {{group}}</span>
      </div>
    </div>
    <app-ranking-results [matchCard]="matchCard$ | async">
    </app-ranking-results>
  `,
    styles: [],
    standalone: false
})
export class RankingResultsContainerComponent implements OnInit, OnDestroy, AfterViewInit {
  matchCard$: Observable<MatchCard>;

  private subscriptions: Subscription = new Subscription();
  returnUrl: string;

  private loading$: Observable<boolean>;

  eventName: string;
  round: number = 0;
  group: number = 1;

  @ViewChild(RankingResultsComponent)
  rankingResultsComponent: RankingResultsComponent;
  private matchCardId: number;
  private playerRankings: any;

  constructor(private matchCardService: MatchCardService,
              private tournamentEventConfigService: TournamentEventConfigService,
              private activatedRoute: ActivatedRoute,
              private linearProgressBarService: LinearProgressBarService) {
    this.matchCardId = this.activatedRoute.snapshot.params['matchCardId'] || 0;
    this.returnUrl = history?.state?.returnUrl || '/ui/home';
    this.eventName = history?.state?.eventName;
  }

  ngOnInit() {
    this.setupProgressIndicator();
    if (this.matchCardId != 0) {
      this.loadMatchCard(this.matchCardId);
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  private setupProgressIndicator() {
    // if any of the service are loading show the loading progress
    this.loading$ = combineLatest(
      this.matchCardService.store.select(this.matchCardService.selectors.selectLoading),
      this.tournamentEventConfigService.store.select(this.tournamentEventConfigService.selectors.selectLoading),
      (matchCardsLoading: boolean, eventInfoLoading: boolean) => {
        return matchCardsLoading || eventInfoLoading;
      }
    );

    const loadingSubscription = this.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
  }

  private loadMatchCard(matchCardId: number) {
    const selector = createSelector(
      this.matchCardService.selectors.selectEntityMap,
      (entityMap) => {
        return entityMap[matchCardId];
      });
    const localMatchCard$ = this.matchCardService.store.select(selector);
    const subscription = localMatchCard$
      .subscribe((matchCard: MatchCard) => {
        if (matchCard == null) {
          // console.log('getting match card from server');
          // get from the server if not cached yet
          this.matchCardService.getByKey(matchCardId);
        } else {
          this.group = matchCard.groupNum;
          this.playerRankings = matchCard.playerRankings;
          this.round = matchCard.round;
          this.matchCard$ = of(matchCard);
          if (this.eventName == null) {
            this.loadEventInformation(matchCard.eventFk);
          }
        }
      });
    this.subscriptions.add(subscription);
  }

  private loadEventInformation(eventId: number) {
    const selector = createSelector(
      this.tournamentEventConfigService.selectors.selectEntityMap,
      (tournamentEvents) => {
        return tournamentEvents[eventId];
      });
    const tournamentEvent$ = this.tournamentEventConfigService.store.select(selector);
    const subscription = tournamentEvent$.subscribe(
      (tournamentEvent: TournamentEvent) => {
        if (tournamentEvent == null) {
          // get from the server if not cached yet
          // this.tournamentEventConfigService.loadTournamentEvents(tournamentId).pipe(first()).subscribe();
          const tournamentId = 0;
          this.tournamentEventConfigService.getByKey(tournamentId, eventId)
            .pipe(
              first(),
              tap((tournamentEvent: TournamentEvent) => {
                this.eventName = tournamentEvent?.name;
              })
            )
            .subscribe();
        }
      });
    this.subscriptions.add(subscription);
  }

  ngAfterViewInit(): void {
    if (this.playerRankings == null) {
      if (this.rankingResultsComponent != null) {
        this.rankingResultsComponent.rankAndAdvance(this.matchCardId);
      }
    }
  }
}
