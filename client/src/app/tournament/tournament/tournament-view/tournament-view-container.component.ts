import {ChangeDetectionStrategy, Component, OnDestroy, OnInit} from '@angular/core';
import {Observable, Subject, Subscription} from 'rxjs';
import {TournamentInfo} from '../../model/tournament-info.model';
import {ActivatedRoute} from '@angular/router';
import {TournamentInfoService} from '../../service/tournament-info.service';
import {TournamentEntryService} from '../../tournament-entry/service/tournament-entry.service';
import {TournamentEntry} from '../../tournament-entry/model/tournament-entry.model';
import {AuthenticationService} from '../../../user/authentication.service';
import {createSelector} from '@ngrx/store';
import {LinearProgressBarService} from '../../../shared/linear-progress-bar/linear-progress-bar.service';
import {TournamentEventConfigService} from '../../tournament-config/tournament-event-config.service';
import {TournamentEvent} from '../../tournament-config/tournament-event.model';

@Component({
  selector: 'app-tournament-view-container',
  template: `
    <app-tournament-view [tournamentInfo]="tournament$ | async"
                         [entryId]="entryId$ | async"
                         [tournamentEvents]="tournamentEvents$ | async"
    >
    </app-tournament-view>
  `,
  styles: [],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TournamentViewContainerComponent implements OnInit, OnDestroy {

  tournament$: Observable<TournamentInfo>;
  entryId$: Subject<number>;
  tournamentEvents$: Observable<TournamentEvent []>;

  private subscriptions: Subscription = new Subscription();

  constructor(private tournamentInfoService: TournamentInfoService,
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
    this.loadTournamentInfo(tournamentId);
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
   * Gets selected tournament from cache or loads it from server
   * @param tournamentId
   * @private
   */
  private loadTournamentInfo(tournamentId) {
    const tournamentInfoSelector = this.tournamentInfoService.selectors.selectEntityMap;
    const selectedTournamentSelector = createSelector(
      tournamentInfoSelector,
      (entityMap) => {
        return entityMap[tournamentId];
      });

    this.tournament$ = this.tournamentInfoService.store.select(selectedTournamentSelector);
    const subscription = this.tournament$.subscribe(
      (tournamentInfo: TournamentInfo) => {
        if (tournamentInfo) {
          // console.log('got tournamentInfo from cache');
          return tournamentInfo;
        } else {
          // console.log('tournamentInfo not in cache. getting from SERVER');
          // not in cache so read it - we don't need to subscribe because it
          // response will go to the store and we alreay subscribed to it in the outer call
          this.tournamentInfoService.getByKey(tournamentId);
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
    this.tournamentEvents$ = this.tournamentEventConfigService.store.select(this.tournamentEventConfigService.selectors.selectEntities);
    const subscription = this.tournamentEventConfigService.loadTournamentEvents(tournamentId)
      .subscribe(
        (events: TournamentEvent[]) => {
          return events;
        },
        (error: any) => {
          console.log ('error loading tournament events ' + JSON.stringify(error));
        }
      );

    this.subscriptions.add(subscription);
  }
}
