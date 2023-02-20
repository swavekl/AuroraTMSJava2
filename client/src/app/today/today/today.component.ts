import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {combineLatest, Observable, Subscription} from 'rxjs';
import {createSelector} from '@ngrx/store';
import {NavigateUtil} from '../../shared/navigate-util';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {TournamentEntryService} from '../../tournament/tournament-entry/service/tournament-entry.service';
import {TournamentInfoService} from '../../tournament/service/tournament-info.service';
import {TournamentInfo} from '../../tournament/model/tournament-info.model';

/**
 * Page for today's task at the tournament
 */
@Component({
  selector: 'app-today',
  templateUrl: './today.component.html',
  styleUrls: ['./today.component.css']
})
export class TodayComponent implements OnInit, OnDestroy {

  private tournamentInfo: TournamentInfo;

  public tournamentId: number;

  public tournamentEntryId: number;

  private tournamentDay: number;

  private loading$: Observable<boolean>;

  private subscriptions: Subscription = new Subscription();

  constructor(private router: Router,
              private activatedRoute: ActivatedRoute,
              private tournamentEntryService: TournamentEntryService,
              private tournamentInfoService: TournamentInfoService,
              private linearProgressBarService: LinearProgressBarService) {
      this.tournamentId = this.activatedRoute.snapshot.params['tournamentId'] || 0;
      this.tournamentDay = this.activatedRoute.snapshot.params['tournamentDay'] || 0;
      this.tournamentEntryId = this.activatedRoute.snapshot.params['tournamentEntryId'] || 0;
      this.setupProgressIndicator();
      this.loadTournamentInfo(this.tournamentId);
      // this.loadPlayerEntry(this.tournamentEntryId);
  }

  ngOnInit(): void {
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
      this.tournamentEntryService.loading$,
      this.tournamentInfoService.loading$,
      (tournamentEntryLoading: boolean, tournamentInfoLoading: boolean) => {
        return tournamentEntryLoading || tournamentInfoLoading;
      }
    );
    const loadingSubscription = this.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
  }

  /**
   *
   * @private
   */
  private loadTournamentInfo(tournamentId: number) {
    // create a selector for fast lookup in cache
    const tournamentInfoSelector = this.tournamentInfoService.selectors.selectEntityMap;
    const selectedTournamentSelector = createSelector(
      tournamentInfoSelector,
      (entityMap) => {
        return entityMap[tournamentId];
      });

    const subscription = this.tournamentInfoService.store.select(selectedTournamentSelector)
      .subscribe(
        (tournamentInfo: TournamentInfo) => {
          if (tournamentInfo) {
            // console.log('tournamentInfo IS in cache.');
            this.tournamentInfo = tournamentInfo;
          } else {
            // console.log('tournamentInfo not in cache. getting from SERVER');
            // not in cache so get it. Since it is an entity collection it will be
            // piped to the above selector and processed by if branch
            this.tournamentInfoService.getByKey(tournamentId);
          }
        });
    this.subscriptions.add(subscription);
  }

  checkInCommunicate() {
    this.router.navigateByUrl(`/ui/today/checkincommunicate/${this.tournamentId}/${this.tournamentDay}/0`);
  }

  directionsToVenue() {
    if (this.tournamentInfo) {
      const url = NavigateUtil.getNavigationURL(this.tournamentInfo.streetAddress, this.tournamentInfo.city,
        this.tournamentInfo.state, this.tournamentInfo.venueName);
      window.open(url);
    }
  }

  todaysSchedule() {
    this.router.navigateByUrl(`/ui/today/playerschedule/${this.tournamentId}/${this.tournamentDay}/${this.tournamentEntryId}`);
  }
}
