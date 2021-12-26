import {Component, OnDestroy, OnInit} from '@angular/core';
import {combineLatest, Observable, of, Subscription} from 'rxjs';
import {first, map} from 'rxjs/operators';
import {TableUsage} from '../model/table-usage.model';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {TableUsageService} from '../service/table-usage.service';
import {TournamentConfigService} from '../../tournament/tournament-config/tournament-config.service';
import {Tournament} from '../../tournament/tournament-config/tournament.model';
import {TodayService} from '../../shared/today.service';
import {MatchCardService} from '../../matches/service/match-card.service';
import {MatchCard} from '../../matches/model/match-card.model';
import {DateUtils} from '../../shared/date-utils';
import {TournamentEventConfigService} from '../../tournament/tournament-config/tournament-event-config.service';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';

@Component({
  selector: 'app-table-usage-container',
  template: `
    <app-table-usage [tableUsageList]="tableUsageList$ | async"
                     [matchCards]="matchCards$ | async"
                     [tournamentEvents]="tournamentEvents$ | async">
    </app-table-usage>
  `,
  styles: []
})
export class TableUsageContainerComponent implements OnInit, OnDestroy {

  tableUsageList$: Observable<TableUsage[]>;

  matchCards$: Observable<MatchCard[]>;

  tournamentEvents$: Observable<TournamentEvent[]>;

  private tournamentId: number;

  private tournamentDay: number;

  private subscriptions: Subscription = new Subscription();
  private loading$: Observable<boolean>;

  constructor(private tableUsageService: TableUsageService,
              private tournamentService: TournamentConfigService,
              private todayService: TodayService,
              private matchCardService: MatchCardService,
              private tournamentEventConfigService: TournamentEventConfigService,
              private linearProgressBarService: LinearProgressBarService) {
    this.setupProgressIndicator();
    this.loadTableUsagesForTodaysTournament();
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  private setupProgressIndicator() {
    this.loading$ = combineLatest(
      this.tournamentService.store.select(this.tournamentService.selectors.selectLoading),
      this.tableUsageService.store.select(this.tableUsageService.selectors.selectLoading),
      this.tournamentEventConfigService.store.select(this.tournamentEventConfigService.selectors.selectLoading),
      this.matchCardService.store.select(this.matchCardService.selectors.selectLoading),
      (tournamentsLoading: boolean, tableUsageLoading: boolean, eventConfigsLoading: boolean, matchCardsLoading: boolean) => {
        return tournamentsLoading || tableUsageLoading || eventConfigsLoading || matchCardsLoading;
      }
    );

    const loadingSubscription = this.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
  }

  ngOnInit(): void {
  }

  private loadTableUsagesForTodaysTournament() {
    const todaysDate: Date = this.todayService.todaysDate;
    const subscription = this.tournamentService.getWithQuery(`today=${todaysDate}`)
      .pipe(
        first(),
        map((todaysTournaments: Tournament[]) => {
          console.log('got todays tournaments', todaysTournaments);
          if (todaysTournaments?.length > 0) {
            const tournament: Tournament = todaysTournaments[0];
            const difference = new DateUtils().daysBetweenDates(tournament.startDate, todaysDate);
            this.tournamentDay = difference + 1;
            this.tournamentId = tournament.id;
            console.log('tournamentid', this.tournamentId);
            this.loadTableUsage(this.tournamentId);
            this.loadEvents(this.tournamentId);
            this.loadMatchCards();
          }
        }))
      .subscribe();
    this.subscriptions.add(subscription);
  }

  private loadTableUsage(tournamentId: number) {
    this.tableUsageList$ = this.tableUsageService.store.select(
      this.tableUsageService.selectors.selectEntities);
    const params = `tournamentId=${tournamentId}`;
    // no need to subscribe since it is subscribed in a template
    this.tableUsageService.getWithQuery(params);
  }

  /**
   * Need event names
   * @param tournamentId
   * @private
   */
  private loadEvents(tournamentId: number) {
    this.tournamentEvents$ = this.tournamentEventConfigService.store.select(
      this.tournamentEventConfigService.selectors.selectEntities);
    // load them - they will surface via this selector
    this.tournamentEventConfigService.loadTournamentEvents(this.tournamentId);
  }

  /**
   *
   * @private
   */
  private loadMatchCards() {
    this.matchCards$ = this.matchCardService.store.select(this.matchCardService.selectors.selectEntities)
      .pipe(
        map((matchCards: MatchCard[]) => {
          if (matchCards?.length > 0) {
            const filteredMatchCards = [];
            for (const matchCard of matchCards) {
              const matchCompleted = (matchCard.playerRankings != null);
              if (!matchCompleted) {
                filteredMatchCards.push(matchCard);
              } else {
                console.log('match completed');
              }
            }
            return filteredMatchCards;
          }
        }));
    this.matchCardService.loadAllForTheTournamentDay(this.tournamentId, this.tournamentDay);
  }
}
