import {Component, OnDestroy, OnInit} from '@angular/core';
import {combineLatest, Observable, Subscription} from 'rxjs';
import {PlayerScheduleItem} from '../model/player-schedule-item.model';
import {PlayerScheduleService} from '../service/player-schedule.service';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {AuthenticationService} from '../../user/authentication.service';
import {ActivatedRoute} from '@angular/router';
import {TournamentInfoService} from '../../tournament/service/tournament-info.service';
import {createSelector} from '@ngrx/store';
import {TournamentInfo} from '../../tournament/model/tournament-info.model';

@Component({
    selector: 'app-player-schedule-container-component',
    template: `
    <app-player-schedule
      [playerScheduleItems]="playerScheduleItems$ | async"
      [tournamentInfo]="tournamentInfo"
      [tournamentDay]="tournamentDay"
      [tournamentEntryId]="tournamentEntryId">
    </app-player-schedule>
  `,
    styles: [],
    standalone: false
})
export class PlayerScheduleContainerComponent implements OnInit, OnDestroy {

  // tournament entry id for this player
  public tournamentEntryId: number;

  public tournamentDay: number;

  public tournamentInfo: TournamentInfo;

  // items representing the schedule of play for this player
  public playerScheduleItems$: Observable<PlayerScheduleItem[]>;

  private loading$: Observable<boolean>;

  private subscriptions: Subscription = new Subscription();

  constructor(private activatedRoute: ActivatedRoute,
              private playerScheduleService: PlayerScheduleService,
              private tournamentInfoService: TournamentInfoService,
              private authenticationService: AuthenticationService,
              private linearProgressBarService: LinearProgressBarService) {
    const tournamentId = this.activatedRoute.snapshot.params['tournamentId'] || 0;
    this.tournamentEntryId = this.activatedRoute.snapshot.params['tournamentEntryId'] || 0;
    this.tournamentDay = this.activatedRoute.snapshot.params['tournamentDay'] || 1;
    this.setupProgressIndicator();
    this.loadTournamentInfo(tournamentId);
    this.loadPlayerSchedule();
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  private setupProgressIndicator() {
    this.loading$ = combineLatest(
      this.playerScheduleService.loading$,
      this.tournamentInfoService.loading$,
      (playerScheduleLoading: boolean, tournamentInfoLoading: boolean) => {
        return playerScheduleLoading || tournamentInfoLoading;
      }
    );
    const subscription = this.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });

    this.subscriptions.add(subscription);
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

  private loadPlayerSchedule() {
    const profileId = this.authenticationService.getCurrentUserProfileId();
    // subscribed by async pipe
    this.playerScheduleItems$ = this.playerScheduleService.getFullPlayerSchedule(this.tournamentEntryId, profileId);
  }
}
