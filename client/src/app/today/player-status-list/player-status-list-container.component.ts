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
import {TournamentEventConfigService} from '../../tournament/tournament-config/tournament-event-config.service';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {CheckInType} from '../../tournament/model/check-in-type.enum';

@Component({
    selector: 'app-player-status-list-container-component',
    template: `
    <app-player-status-list
      [tournamentId]="tournamentId"
      [tournamentName]="tournamentName"
      [tournamentDay]="tournamentDay"
      [tournamentDuration]="tournamentDuration"
      [checkInType]="checkInType"
      [playerStatusList]="playerStatusList$ | async"
      [entryInfos]="entryInfos$ | async"
      [tournamentEvents]="tournamentEvents$ | async"
      (eventEmitter)="onEvent($event)">
    </app-player-status-list>
  `,
    styles: [],
    standalone: false
})
export class PlayerStatusListContainerComponent implements OnDestroy {

  tournamentId: number;

  tournamentName: string;

  tournamentDay: number;

  tournamentDuration: number;

  playerStatusList$: Observable<PlayerStatus[]>;
  entryInfos$: Observable<TournamentEntryInfo[]>;

  tournamentEvents$: Observable<TournamentEvent[]>;

  checkInType: CheckInType;

  private loading$: Observable<boolean>;
  private subscriptions: Subscription = new Subscription();

  constructor(private activatedRoute: ActivatedRoute,
              private playerStatusService: PlayerStatusService,
              private tournamentEntryInfoService: TournamentEntryInfoService,
              private todayService: TodayService,
              private tournamentEventConfigService: TournamentEventConfigService,
              private linearProgressBarService: LinearProgressBarService) {
    const strTournamentId = this.activatedRoute.snapshot.params['tournamentId'] || 0;
    this.tournamentId = Number(strTournamentId);
    this.tournamentName = history?.state?.tournamentName || '';
    const tournamentStartDate = history?.state?.tournamentStartDate;
    const tournamentEndDate = history?.state?.tournamentEndDate;
    this.checkInType = history?.state?.checkInType;
    this.tournamentDuration = 1;
    if (tournamentStartDate != null && tournamentEndDate != null) {
      this.tournamentDuration += new DateUtils().daysBetweenDates(tournamentStartDate, tournamentEndDate);
    }
    if (tournamentStartDate != null) {
      const today = this.todayService.todaysDate;
      const difference = new DateUtils().daysBetweenDates(tournamentStartDate, today);
      this.tournamentDay = difference + 1;
      if (this.tournamentDay <= 0 || this.tournamentDay > this.tournamentDuration) {
        this.tournamentDay = 1;
      }
    } else {
      this.tournamentDay = 1;
    }
    this.setupProgressIndicator();
    this.loadAllPlayersStatus(this.tournamentId);
    this.loadTournamentEntries(this.tournamentId);
    this.loadTournamentEvents(this.tournamentId);
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
    let params = `tournamentId=${tournamentId}&tournamentDay=0`;
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

  loadTournamentEvents(tournamentId: number) {
    this.tournamentEvents$ = this.tournamentEventConfigService.store.select(
      this.tournamentEventConfigService.selectors.selectEntities);
    // load them - they will surface via this selector
    this.tournamentEventConfigService.loadTournamentEvents(tournamentId);
  }
}

