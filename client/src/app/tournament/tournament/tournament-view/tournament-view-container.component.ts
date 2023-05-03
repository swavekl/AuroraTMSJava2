import {ChangeDetectionStrategy, Component, OnDestroy, OnInit} from '@angular/core';
import {Observable, Subject, Subscription} from 'rxjs';
import {ActivatedRoute} from '@angular/router';
import {TournamentEntryService} from '../../tournament-entry/service/tournament-entry.service';
import {TournamentEntry} from '../../tournament-entry/model/tournament-entry.model';
import {AuthenticationService} from '../../../user/authentication.service';
import {createSelector} from '@ngrx/store';
import {LinearProgressBarService} from '../../../shared/linear-progress-bar/linear-progress-bar.service';
import {TournamentEventConfigService} from '../../tournament-config/tournament-event-config.service';
import {TournamentEvent} from '../../tournament-config/tournament-event.model';
import {Tournament} from '../../tournament-config/tournament.model';
import {TournamentConfigService} from '../../tournament-config/tournament-config.service';

@Component({
  selector: 'app-tournament-view-container',
  template: `
    <app-tournament-view [tournament]="tournament$ | async"
                         [entryId]="entryId$ | async"
                         [tournamentEvents]="tournamentEvents$ | async"
    >
    </app-tournament-view>
  `,
  styles: [],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TournamentViewContainerComponent implements OnInit, OnDestroy {

  // tournament configuration
  tournament$: Observable<Tournament>;

  // this player's entry into this event if exists
  entryId$: Subject<number>;

  // tournament events
  tournamentEvents$: Observable<TournamentEvent []>;

  private subscriptions: Subscription = new Subscription();

  private reloadTournament: boolean = false;

  constructor(private tournamentConfigService: TournamentConfigService,
              private tournamentEntryService: TournamentEntryService,
              private tournamentEventConfigService: TournamentEventConfigService,
              private authService: AuthenticationService,
              private activatedRoute: ActivatedRoute,
              private linearProgressBarService: LinearProgressBarService) {
    this.entryId$ = new Subject<number>();
  }

  ngOnInit(): void {
    const loadingSubscription = this.tournamentEntryService.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);

    const tournamentId = this.activatedRoute.snapshot.params['id'] || 0;
    this.reloadTournament = this.activatedRoute.snapshot.queryParamMap.get('reload') === 'true';
    this.loadTournament(tournamentId);
    this.loadEntryIfExists(tournamentId);
    this.loadTournamentEvents(tournamentId);
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  /**
   * Loads player's tournament entry if it exists
   * @param tournamentId
   * @private
   */
  private loadEntryIfExists(tournamentId: number) {
    const profileId = this.authService.getCurrentUserProfileId();
    const params = `tournamentId=${tournamentId}&profileId=${profileId}`;
    const tournamentEntry$: Observable<TournamentEntry[]> = this.tournamentEntryService.getWithQuery(params);
    const subscription: Subscription = tournamentEntry$.subscribe((tournamentEntries: TournamentEntry[]) => {
      // console.log ('got tournament entries', tournamentEntries);
      const entryId: number = (tournamentEntries.length > 0) ? tournamentEntries[0].id : 0;
      this.entryId$.next(entryId);
    }, error => {
      this.entryId$.next(0);
    });
    this.subscriptions.add(subscription);
  }

  /**
   * Gets tournament configuration
   * @param tournamentId
   * @private
   */
  private loadTournament(tournamentId: number) {
    const selectedTournamentSelector = createSelector(
      this.tournamentConfigService.selectors.selectEntityMap,
      (entityMap) => {
        return entityMap[tournamentId];
      });
    // tournament information will not change just get it once
    this.tournament$ = this.tournamentConfigService.store.select(selectedTournamentSelector);
    const subscription = this.tournament$.subscribe((tournament: Tournament) => {
      if (!tournament || this.reloadTournament) {
        this.reloadTournament = false;
        this.tournamentConfigService.getByKey(tournamentId);
      }
    });
    this.subscriptions.add(subscription);
  }

  /**
   * Loads events of the specified tournament
   * @param tournamentId
   * @private
   */
  private loadTournamentEvents(tournamentId: number) {
    this.tournamentEvents$ = this.tournamentEventConfigService.store.select(
      this.tournamentEventConfigService.selectors.selectEntities);
    this.tournamentEventConfigService.loadTournamentEvents(tournamentId);
  }
}
