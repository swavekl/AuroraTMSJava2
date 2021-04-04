import {Component, OnDestroy, OnInit} from '@angular/core';
import {TournamentEntryInfoService} from '../../service/tournament-entry-info.service';
import {ActivatedRoute} from '@angular/router';
import {combineLatest, Observable, of, Subscription} from 'rxjs';
import {LinearProgressBarService} from '../../../shared/linear-progress-bar/linear-progress-bar.service';
import {first, map} from 'rxjs/operators';
import {TournamentEntryInfo} from '../../model/tournament-entry-info.model';
import {TournamentEvent} from '../../tournament-config/tournament-event.model';
import {TournamentEventConfigService} from '../../tournament-config/tournament-event-config.service';
import {createSelector} from '@ngrx/store';
import {TournamentInfo} from '../../model/tournament-info.model';
import {TournamentInfoService} from '../../service/tournament-info.service';
import {DateUtils} from '../../../shared/date-utils';

@Component({
  selector: 'app-tournament-player-list-container',
  template: `
    <app-tournament-players-list [entryInfos]="entryInfos$ | async"
                                 [tournamentEvents]="tournamentEvents$ | async"
                                 [tournamentStartDate]="tournamentStartDate$ | async"
    ></app-tournament-players-list>
  `,
  styles: []
})
export class TournamentPlayersListContainerComponent implements OnInit, OnDestroy {

  private subscriptions: Subscription = new Subscription();

  entryInfos$: Observable<TournamentEntryInfo[]>;
  tournamentEvents$: Observable<TournamentEvent[]>;
  tournamentStartDate$: Observable<Date>;

  loading$: Observable<boolean>;

  constructor(private tournamentEntryInfoService: TournamentEntryInfoService,
              private tournamentInfoService: TournamentInfoService,
              private tournamentEventConfigService: TournamentEventConfigService,
              private activatedRoute: ActivatedRoute,
              private linearProgressBarService: LinearProgressBarService) {
    this.setupProgressIndicator();
  }

  /**
   *
   * @private
   */
  private setupProgressIndicator() {
    // if any of the service are loading show the loading progress
    this.loading$ = combineLatest(
      this.tournamentEntryInfoService.loading$,
      this.tournamentEventConfigService.store.select(this.tournamentEventConfigService.selectors.selectLoading),
      (entryInfosLoading: boolean, eventConfigsLoading: boolean) => {
        return entryInfosLoading || eventConfigsLoading;
      }
    );

    const loadingSubscription = this.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });

    this.subscriptions.add(loadingSubscription);
  }

  ngOnInit(): void {
    const tournamentId = this.activatedRoute.snapshot.params['id'] || 0;
    this.loadTournamentStartDate(tournamentId);
    this.loadTournamentEntries(tournamentId);
    this.loadTournamentEvents(tournamentId);
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }


  private loadTournamentEntries(tournamentId: number) {
    this.entryInfos$ = this.tournamentEntryInfoService.getAll(tournamentId);
    const subscription = this.entryInfos$
      .pipe(first())
      .subscribe(
        (infos: TournamentEntryInfo[]) => {
          return infos;
        },
        (error: any) => {
          console.log('error loading entry infos' + JSON.stringify(error));
        }
      );
    this.subscriptions.add(subscription);
  }

  /**
   * Categorizes entries by event
   * @param tournamentId
   * @private
   */
  private loadTournamentEvents(tournamentId: number) {
    this.tournamentEvents$ = this.tournamentEventConfigService.store.select(
      this.tournamentEventConfigService.selectors.selectEntities);

    const subscription = this.tournamentEvents$
      .pipe(first())
      .subscribe(
        (events: TournamentEvent[]) => {
          if (events != null && events.length > 0) {
            this.tournamentEvents$ = of(events);
            return events;
          } else {
            // don't have event configs cached - load them
            this.tournamentEventConfigService.loadTournamentEvents(tournamentId)
              .pipe(
                first(),
                map(
                  (tournamentEvents: TournamentEvent[]) => {
                    return tournamentEvents;
                  },
                  (error: any) => {
                    console.log('error loading tournament events ' + JSON.stringify(error));
                  }
                ))
              .subscribe();
          }
        },
        (error: any) => {
          console.log('error loading tournament events ' + JSON.stringify(error));
        }
      );

    this.subscriptions.add(subscription);
  }

  /**
   * Gets tournament start date
   * @param tournamentId
   * @private
   */
  private loadTournamentStartDate(tournamentId: number) {
    // tournament view may have passed us the tournament start date
    // but if user navigated to this screen by url then go to the server and get it.
    const strTournamentStartDate = history?.state?.tournamentStartDate;
    if (strTournamentStartDate != null) {
      const tournamentStartDate = new DateUtils().convertFromString(strTournamentStartDate);
      this.tournamentStartDate$ = of(tournamentStartDate);
    } else {
      const tournamentInfoSelector = this.tournamentInfoService.selectors.selectEntityMap;
      const selectedTournamentSelector = createSelector(
        tournamentInfoSelector,
        (entityMap) => {
          return entityMap[tournamentId];
        });

      const tournamentInfo$: Observable<TournamentInfo> = this.tournamentInfoService.store.select(selectedTournamentSelector);
      const subscription = tournamentInfo$
        .subscribe(
          (tournamentInfo: TournamentInfo) => {
            if (tournamentInfo) {
              // console.log('got tournamentInfo from cache for START_DATE');
              const tournamentStartDate2 = new DateUtils().convertFromString(tournamentInfo.startDate);
              this.tournamentStartDate$ = of(tournamentStartDate2);
            } else {
              // console.log('tournamentInfo not in cache. getting from SERVER');
              // not in cache so read it
              this.tournamentInfoService.getByKey(tournamentId);
            }
          });
      this.subscriptions.add(subscription);
    }
  }
}
