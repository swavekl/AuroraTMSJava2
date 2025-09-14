import {Component, OnDestroy, OnInit} from '@angular/core';
import {combineLatest, Observable, of, Subscription} from 'rxjs';
import {first, switchMap, tap} from 'rxjs/operators';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {Tournament} from '../../tournament/tournament-config/tournament.model';
import {TournamentConfigService} from '../../tournament/tournament-config/tournament-config.service';
import {TodayService} from '../../shared/today.service';
import {TournamentEventConfigService} from '../../tournament/tournament-config/tournament-event-config.service';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {MatchCardService} from '../../matches/service/match-card.service';
import {DrawMethod} from '../../tournament/tournament-config/model/draw-method.enum';
import {MatchCard} from '../../matches/model/match-card.model';
import {DateUtils} from '../../shared/date-utils';

@Component({
    selector: 'app-prize-list-container',
    template: `
    <app-prize-list
      [events]="tournamentEvents$ | async"
      [finishedRRMatchCards]="finishedRRMatchCards$ | async">
    </app-prize-list>
  `,
    styles: [],
    standalone: false
})
export class PrizeListContainerComponent implements OnInit, OnDestroy {

  private loading$: Observable<boolean>;

  tournamentEvents$: Observable<TournamentEvent[]>;

  finishedRRMatchCards$: Observable<MatchCard[]>;

  private tournamentId: number;

  private subscriptions: Subscription = new Subscription();

  constructor(private tournamentConfigService: TournamentConfigService,
              private linearProgressBarService: LinearProgressBarService,
              private todayService: TodayService,
              private tournamentEventConfigService: TournamentEventConfigService,
              private matchCardService: MatchCardService) {
    this.setupProgressIndicator();
    this.loadTodaysTournamentEvents();
  }

  ngOnInit(): void {
    this.tournamentConfigService.getAll();
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  /**
   *
   * @private
   */
  private setupProgressIndicator() {
    this.loading$ = combineLatest(
      this.tournamentConfigService.store.select(this.tournamentConfigService.selectors.selectLoading),
      this.tournamentEventConfigService.store.select(this.tournamentEventConfigService.selectors.selectLoading),
      this.matchCardService.store.select(this.matchCardService.selectors.selectLoading),
      (loadingTournament: boolean, loadingEvents: boolean, loadingMatchCards: boolean) => {
        return loadingTournament || loadingEvents || loadingMatchCards;
      }
    );

    const loadingSubscription = this.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
  }

  private loadTodaysTournamentEvents() {
    const todaysDate: Date = this.todayService.todaysDate;
    const todaysDateUtc = new DateUtils().convertFromLocalToUTCDate(todaysDate);
    this.tournamentEventConfigService.clearCache();
    this.tournamentEvents$ = this.tournamentEventConfigService.entities$;
    let subscription = this.tournamentConfigService.getTodaysTournaments(todaysDateUtc)
      .pipe(
        switchMap((todaysTournaments: Tournament[]) => {
            if (todaysTournaments?.length > 0) {
              this.tournamentId = todaysTournaments[0].id;
              // this will be subscribed by the template
              return this.tournamentEventConfigService.loadTournamentEvents(this.tournamentId)
                .pipe(
                  first(),
                  tap((events: TournamentEvent[]) => {
                    this.getFinishedRRMatchCards(events);
                })
                 );
            } else {
              return of([]);
            }
          }
        )
      ).subscribe();
      this.subscriptions.add(subscription);
  }

  private getFinishedRRMatchCards(events: TournamentEvent[]) {
    let finishedMatchCards = [];
    for (let i = 0; i < events.length; i++) {
      const event = events[i];
      if (event.drawMethod === DrawMethod.DIVISION && event.playersToAdvance === 0) {
        this.matchCardService.loadForEvent(event.id, this.tournamentId, true)
          .pipe(
            first(),
            tap((matchCards: MatchCard[]) => {
              finishedMatchCards = finishedMatchCards.concat(matchCards);
              this.finishedRRMatchCards$ = of(finishedMatchCards);
            })).subscribe();
      }
    }
  }
}
