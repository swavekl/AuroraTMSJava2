import {ChangeDetectionStrategy, Component, OnDestroy, OnInit} from '@angular/core';
import {Observable, Subject, Subscription} from 'rxjs';
import {TournamentInfo} from '../tournament-info.model';
import {ActivatedRoute} from '@angular/router';
import {TournamentInfoService} from '../tournament-info.service';
import {TournamentEntryService} from '../../tournament-entry/service/tournament-entry.service';
import {TournamentEntry} from '../../tournament-entry/model/tournament-entry.model';
import {AuthenticationService} from '../../../user/authentication.service';
import {createSelector} from '@ngrx/store';
import {LinearProgressBarService} from '../../../shared/linear-progress-bar/linear-progress-bar.service';

@Component({
  selector: 'app-tournament-view-container',
  template: `
    <app-tournament-view [tournament]="tournament$ | async" [entryId]="entryId$ | async">
    </app-tournament-view>
  `,
  styles: [],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TournamentViewContainerComponent implements OnInit, OnDestroy {

  tournament$: Observable<TournamentInfo>;
  entryId$: Subject<number>;

  private subscriptions: Subscription = new Subscription();

  constructor(private tournamentInfoService: TournamentInfoService,
              private tournamentEntryService: TournamentEntryService,
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
console.log ('getting tournament info for tournament ', tournamentId);
    const tournamentInfoSelector = this.tournamentInfoService.selectors.selectEntityMap;
    const selectedTournamentSelector = createSelector(
      tournamentInfoSelector,
      (entityMap) => {
        return entityMap[tournamentId];
      });

    this.tournament$ = this.tournamentInfoService.store.select(selectedTournamentSelector);
    const profileId = this.authService.getCurrentUserProfileId();
    const params = `tournamentId=${tournamentId}&profileId=${profileId}`;
    const tournamentEntry$: Observable<TournamentEntry[]> = this.tournamentEntryService.getWithQuery(params);
    const subscription: Subscription = tournamentEntry$.subscribe( (tournamentEntries: TournamentEntry[]) => {
      // console.log ('got tournament entries', tournamentEntries);
      const entryId: number = (tournamentEntries.length > 0) ? tournamentEntries[0].id : 0;
      console.log ('got entryId ', entryId);
      this.entryId$.next(entryId);
    }, error => {
      this.entryId$.next(0);
    });
    this.subscriptions.add(subscription);
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

}
