import {Component, OnDestroy, OnInit} from '@angular/core';
import {combineLatest, Observable, Subscription} from 'rxjs';
import {first, map} from 'rxjs/operators';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {Tournament} from '../../tournament/tournament-config/tournament.model';
import {TournamentConfigService} from '../../tournament/tournament-config/tournament-config.service';
import {TodayService} from '../../shared/today.service';
import {TournamentEventConfigService} from '../../tournament/tournament-config/tournament-event-config.service';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';

@Component({
  selector: 'app-prize-list-container',
  template: `
    <app-prize-list [events]="tournamentEvents$ | async">
      prize-list-container works!
    </app-prize-list>
  `,
  styles: []
})
export class PrizeListContainerComponent implements OnInit, OnDestroy {

  private loading$: Observable<boolean>;

  tournamentEvents$: Observable<TournamentEvent[]>;

  private subscriptions: Subscription = new Subscription();

  constructor(private tournamentConfigService: TournamentConfigService,
              private linearProgressBarService: LinearProgressBarService,
              private todayService: TodayService,
              private tournamentEventConfigService: TournamentEventConfigService) {
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
      (loadingTournament: boolean, loadingEvents: boolean) => {
        return loadingTournament || loadingEvents;
      }
    );

    const loadingSubscription = this.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
  }

  private loadTodaysTournamentEvents() {
    const todaysDate: Date = this.todayService.todaysDate;
    const subscription = this.tournamentConfigService.getTodaysTournaments(todaysDate)
      .pipe(
        first(),
        map((todaysTournaments: Tournament[]) => {
            if (todaysTournaments?.length > 0) {
              const tournamentId = todaysTournaments[0].id;
              // this will be subscribe by the template
              this.tournamentEvents$ = this.tournamentEventConfigService.loadTournamentEvents(tournamentId);
            }
          }
        )
      ).subscribe();
    this.subscriptions.add(subscription);
  }
}
