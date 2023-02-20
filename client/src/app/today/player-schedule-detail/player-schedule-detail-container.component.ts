import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {combineLatest, Observable, Subscription} from 'rxjs';
import {PlayerScheduleService} from '../service/player-schedule.service';
import {PlayerScheduleItem} from '../model/player-schedule-item.model';
import {createSelector} from '@ngrx/store';
import {TournamentInfo} from '../../tournament/model/tournament-info.model';
import {TournamentInfoService} from '../../tournament/service/tournament-info.service';
import {CheckInType} from '../../tournament/model/check-in-type.enum';

@Component({
  selector: 'app-player-schedule-detail-container',
  template: `
    <app-player-schedule-detail [playerScheduleItem]="playerScheduleItem$ | async"
                                [checkInType]="checkInType"
    [returnUrl]="returnUrl"
    [tournamentId]="tournamentId">
    </app-player-schedule-detail>
  `,
  styles: [
  ]
})
export class PlayerScheduleDetailContainerComponent implements OnInit, OnDestroy {

  public playerScheduleItem$: Observable<PlayerScheduleItem>;

  public returnUrl: string;

  private subscriptions: Subscription = new Subscription();

  public tournamentId: number;

  loading$: Observable<boolean>;

  public checkInType: CheckInType;

  constructor(private activatedRoute: ActivatedRoute,
              private linearProgressBarService: LinearProgressBarService,
              private playerScheduleService: PlayerScheduleService,
              private tournamentInfoService: TournamentInfoService) {
    this.tournamentId = this.activatedRoute.snapshot.params['tournamentId'] || 0;
    const matchCardId = this.activatedRoute.snapshot.params['matchCardId'] || 0;
    this.returnUrl = history.state?.returnUrl || '/ui/home';
    this.setupProgressIndicator();
    this.loadPlayerScheduleDetail(matchCardId);
    this.loadTournamentInfo(this.tournamentId);
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
      (tournamentEntryLoading: boolean, tournamentInfoLoading: boolean) => {
        return tournamentEntryLoading || tournamentInfoLoading;
      }
    );
    const loadingSubscription = this.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
  }

  private loadPlayerScheduleDetail(matchCardId: number) {
    this.playerScheduleItem$ = this.playerScheduleService.getPlayerScheduleDetail(matchCardId);
  }

  /**
   *
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

    const tournamentInfo$ = this.tournamentInfoService.store.select(selectedTournamentSelector);
    const subscription = tournamentInfo$.subscribe((tournamentInfo: TournamentInfo) => {
      if (tournamentInfo) {
        console.log('got tournament info from cache');
        this.checkInType = tournamentInfo.checkInType;
      } else {
        console.log('tournamentInfo not in cache. getting from SERVER');
        // not in cache so get it. Since it is an entity collection it will be
        // piped to the above selector and processed by if branch
        this.tournamentInfoService.getByKey(tournamentId);
      }
    });
    this.subscriptions.add(subscription);
  }

}
