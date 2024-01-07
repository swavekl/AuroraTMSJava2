import {Component, OnDestroy} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {first, switchMap} from 'rxjs/operators';
import {combineLatest, Observable, of, Subscription} from 'rxjs';

import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {PlayerStatusService} from '../service/player-status.service';
import {PlayerStatus} from '../model/player-status.model';
import {TournamentEntryInfo} from '../../tournament/model/tournament-entry-info.model';
import {TournamentEntryInfoService} from '../../tournament/service/tournament-entry-info.service';
import {TodayService} from '../../shared/today.service';
import {DateUtils} from '../../shared/date-utils';

@Component({
  selector: 'app-player-status-list-container-component',
  template: `
    <app-player-status-list
      [tournamentId]="tournamentId"
      [tournamentName]="tournamentName"
      [tournamentDay]="tournamentDay"
      [playerStatusList]="playerStatusList$ | async"
      [entryInfos]="entryInfos$ | async"
      (eventEmitter)="onEvent($event)">
    </app-player-status-list>
  `,
  styles: []
})
export class PlayerStatusListContainerComponent implements OnDestroy {

  tournamentId: number;

  tournamentName: string;

  tournamentDay: number;

  playerStatusList$: Observable<PlayerStatus[]>;
  entryInfos$: Observable<TournamentEntryInfo[]>;

  private loading$: Observable<boolean>;
  private subscriptions: Subscription = new Subscription();

  constructor(private activatedRoute: ActivatedRoute,
              private playerStatusService: PlayerStatusService,
              private tournamentEntryInfoService: TournamentEntryInfoService,
              private todayService: TodayService,
              private linearProgressBarService: LinearProgressBarService) {
    const strTournamentId = this.activatedRoute.snapshot.params['tournamentId'] || 0;
    this.tournamentId = Number(strTournamentId);
    this.tournamentName = history?.state?.tournamentName || '';
    const tournamentStartDate = history?.state?.tournamentStartDate;
    if (tournamentStartDate != null) {
      const today = this.todayService.todaysDate;
      const difference = new DateUtils().daysBetweenDates(tournamentStartDate, today);
      this.tournamentDay = difference + 1;
    } else {
      this.tournamentDay = 1;
    }
    this.setupProgressIndicator();
    this.loadAllPlayersStatus(this.tournamentId);
    this.loadTournamentEntries(this.tournamentId);
  }

  private setupProgressIndicator() {
    // if any of the service are loading show the loading progress
    this.loading$ = combineLatest([
        this.playerStatusService.store.select(this.playerStatusService.selectors.selectLoading),
        this.tournamentEntryInfoService.loading$,
      ],
      (statusLoading: boolean, entryInfosLoading: boolean) => {
        return statusLoading || entryInfosLoading;
      }
    );

    const loadingSubscription = this.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
  }

  private loadAllPlayersStatus(tournamentId: number) {
    // this is subscribed by template | async
    this.playerStatusService.clearCache();
    this.playerStatusList$ = this.playerStatusService.entities$;
    let params = `tournamentId=${tournamentId}&tournamentDay=${this.tournamentDay}`;
    this.playerStatusService.loadWithQuery(params);
  }

  private loadTournamentEntries(tournamentId: number) {
    const subscription = this.tournamentEntryInfoService.getAll(tournamentId)
      .pipe(
        first())
      .subscribe(
        (infos: TournamentEntryInfo[]) => {
          this.entryInfos$ = of(infos);
        },
        (error: any) => {
          console.log('error loading entry infos' + JSON.stringify(error));
        }
      );
    this.subscriptions.add(subscription);
  }


  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  onEvent($event: any) {
    if ($event.action === 'ok') {
      const playerStatus = {...$event.playerStatus, tournamentDay: this.tournamentDay};
      this.playerStatusService.upsert(playerStatus)
        .pipe(
          switchMap((updatedPlayerStatus: PlayerStatus) => {
            let params = `tournamentId=${this.tournamentId}&tournamentDay=${this.tournamentDay}`;
            return this.playerStatusService.loadWithQuery(params);
          }));
    }
  }
}

