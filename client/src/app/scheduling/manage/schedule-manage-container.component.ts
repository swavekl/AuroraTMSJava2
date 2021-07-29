import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {combineLatest, Observable, of, Subscription} from 'rxjs';
import {Tournament} from '../../tournament/tournament-config/tournament.model';
import {createSelector} from '@ngrx/store';
import {TournamentConfigService} from '../../tournament/tournament-config/tournament-config.service';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {MatchCardService} from '../../matches/service/match-card.service';
import {MatchCard} from '../../matches/model/match-card.model';
import {MatchSchedulingService} from '../service/match-scheduling.service';
import {first} from 'rxjs/operators';

@Component({
  selector: 'app-schedule-manage-container',
  template: `
    <app-schedule-manage
      [tournament]="tournament$ | async"
      [matchCards]="matchCards$ | async"
      (dayChangedEvent)="onDayChangedEvent($event)"
      (generateScheduleForEvent)="onGenerateForDay($event)"
    >
    </app-schedule-manage>
  `,
  styles: []
})
export class ScheduleManageContainerComponent implements OnInit, OnDestroy {

  // tournament information
  tournament$: Observable<Tournament>;

  // match cards for selected event
  matchCards$: Observable<MatchCard[]>;

  private subscriptions: Subscription = new Subscription();

  private loading$: Observable<boolean>;

  private tournamentId: number;

  constructor(private activatedRoute: ActivatedRoute,
              private tournamentConfigService: TournamentConfigService,
              private matchCardService: MatchCardService,
              private matchSchedulingService: MatchSchedulingService,
              private linearProgressBarService: LinearProgressBarService) {
    const strTournamentId = this.activatedRoute.snapshot.params['tournamentId'] || 0;
    this.tournamentId = Number(strTournamentId);
    this.setupProgressIndicator();
    this.loadTournament(this.tournamentId);
    this.setupMatchCards(this.tournamentId);
  }

  private setupProgressIndicator() {
    // if any of the service are loading show the loading progress
    this.loading$ = combineLatest(
      this.tournamentConfigService.store.select(this.tournamentConfigService.selectors.selectLoading),
      this.matchCardService.store.select(this.matchCardService.selectors.selectLoading),
      (tournamentConfigLoading: boolean, matchCardsLoading: boolean) => {
        return tournamentConfigLoading || matchCardsLoading;
      }
    );

    const loadingSubscription = this.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  private loadTournament(tournamentId: number) {
    const selectedTournamentSelector = createSelector(
      this.tournamentConfigService.selectors.selectEntityMap,
      (entityMap) => {
        return entityMap[tournamentId];
      });
    // tournament information will not change just get it once
    this.tournament$ = this.tournamentConfigService.store.select(selectedTournamentSelector);
    const subscription = this.tournament$.subscribe((tournament: Tournament) => {
      if (!tournament) {
        this.tournamentConfigService.getByKey(tournamentId);
      }
    });
    this.subscriptions.add(subscription);
  }

  private setupMatchCards(tournamentId: number) {
    this.matchCards$ = this.matchCardService.store.select(this.matchCardService.selectors.selectEntities);
    this.loadMatchCardsForDay(1);
  }

  private loadMatchCardsForDay(day: number) {
    this.matchCardService.loadAllForTheTournamentDay(this.tournamentId, day);
  }

  onDayChangedEvent(day: number) {
    this.loadMatchCardsForDay(day);
  }

  /**
   *
   * @param day
   */
  onGenerateForDay(day: number) {
    const subscription = this.matchSchedulingService.generateScheduleForTournamentAndDay(this.tournamentId, day)
      .pipe(first())
      .subscribe(
        (matchCards: MatchCard[]) => {
          this.matchCards$ = of(matchCards);
        }, (error: any) => {
          console.log('error ', error);
        });
    this.subscriptions.add(subscription);
  }
}
