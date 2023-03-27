import {Component, OnDestroy, OnInit} from '@angular/core';
import {DrawService} from '../../draws-common/service/draw.service';
import {ActivatedRoute} from '@angular/router';
import {LinearProgressBarService} from '../../../shared/linear-progress-bar/linear-progress-bar.service';
import {combineLatest, Observable, of, Subscription} from 'rxjs';
import {TournamentEvent} from '../../../tournament/tournament-config/tournament-event.model';
import {TournamentEventConfigService} from '../../../tournament/tournament-config/tournament-event-config.service';
import {createSelector} from '@ngrx/store';
import {Tournament} from '../../../tournament/tournament-config/tournament.model';
import {TournamentConfigService} from '../../../tournament/tournament-config/tournament-config.service';
import {DateUtils} from '../../../shared/date-utils';

@Component({
  selector: 'app-draws-view-container',
  template: `
      <app-draws-view [tournamentEvents]="tournamentEvents$ | async"
                      [tournamentStartDate]="tournamentStartDate$ | async"
      >
      </app-draws-view>
  `,
  styles: []
})
export class DrawsViewEventsContainerComponent implements OnInit, OnDestroy {

  tournamentEvents$: Observable<TournamentEvent[]>;

  private tournamentId: number;

  tournamentStartDate$: Observable<Date>;

  private loading$: Observable<boolean>;

  private subscriptions: Subscription = new Subscription();

  constructor(private activatedRoute: ActivatedRoute,
              private drawService: DrawService,
              private tournamentEventConfigService: TournamentEventConfigService,
              private tournamentConfigService: TournamentConfigService,
              private linearProgressBarService: LinearProgressBarService) {
    const strTournamentId = this.activatedRoute.snapshot.params['tournamentId'] || 0;
    this.tournamentId = Number(strTournamentId);
    this.setupProgressIndicator();
    this.loadTournament(this.tournamentId);
    this.loadTournamentEvents(this.tournamentId);
  }

  /**
   *
   * @private
   */
  private setupProgressIndicator() {
    // if any of the service are loading show the loading progress
    this.loading$ = combineLatest([
        this.tournamentEventConfigService.store.select(this.tournamentEventConfigService.selectors.selectLoading),
        this.tournamentConfigService.store.select(this.tournamentConfigService.selectors.selectLoading),
        this.drawService.store.select(this.drawService.selectors.selectLoading)
      ],
      (eventConfigsLoading: boolean, tournamentLoading, drawsLoading: boolean) => {
        return eventConfigsLoading || tournamentLoading || drawsLoading;
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

  private loadTournamentEvents(tournamentId: number) {
    this.tournamentEvents$ = this.tournamentEventConfigService.store.select(
      this.tournamentEventConfigService.selectors.selectEntities);
    // load them - they will surface via this selector
    this.tournamentEventConfigService.loadTournamentEvents(tournamentId);
  }

  private loadTournament(tournamentId: number) {
    const selectedTournamentSelector = createSelector(
      this.tournamentConfigService.selectors.selectEntityMap,
      (entityMap) => {
        return entityMap[tournamentId];
      });
    const localTournament$ = this.tournamentConfigService.store.select(selectedTournamentSelector);
    const subscription = localTournament$
      .subscribe((tournament: Tournament) => {
        if (!tournament) {
          this.tournamentConfigService.getByKey(tournamentId);
        } else {
          this.tournamentStartDate$ = of(new DateUtils().convertFromString(tournament.startDate));
        }
      });
    this.subscriptions.add(subscription);
  }
}
