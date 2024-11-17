import {Component, OnDestroy, OnInit} from '@angular/core';
import {TournamentEntryInfoService} from '../../service/tournament-entry-info.service';
import {ActivatedRoute} from '@angular/router';
import {combineLatest, Observable, of, Subscription} from 'rxjs';
import {LinearProgressBarService} from '../../../shared/linear-progress-bar/linear-progress-bar.service';
import {filter, first, repeat, take} from 'rxjs/operators';
import {TournamentEntryInfo} from '../../model/tournament-entry-info.model';
import {TournamentEvent} from '../../tournament-config/tournament-event.model';
import {TournamentEventConfigService} from '../../tournament-config/tournament-event-config.service';
import {createSelector} from '@ngrx/store';
import {TournamentInfo} from '../../model/tournament-info.model';
import {TournamentInfoService} from '../../service/tournament-info.service';
import {DateUtils} from '../../../shared/date-utils';
import {AuthenticationService} from '../../../user/authentication.service';
import {HttpClient} from '@angular/common/http';

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
              private linearProgressBarService: LinearProgressBarService,
              private authenticationService: AuthenticationService,
              private httpClient: HttpClient) {
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
    const strTournamentId = this.activatedRoute.snapshot.params['id'];
    const tournamentId = (strTournamentId != null) ? Number(strTournamentId) : 0;
    this.loadTournamentStartDate(tournamentId);
    this.loadTournamentEntries(tournamentId);
    this.loadTournamentEvents(tournamentId);
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }


  private loadTournamentEntries(tournamentId: number) {
    // we are reading all tournament entries but to speed up drawing we will 'release' them
    // in chunks of 50 every 50 ms.  This way the first 50 can be painted fast while the remaining
    // chunks can be painted later.  This will eliminate the delay in painting, but it will make the scroll bar
    // get shorter as more items arrive and the list needs to be repainted
    // lazyArray<TournamentEntryInfo>(50, 50)
    const subscription = this.tournamentEntryInfoService.getAll(tournamentId)
      .pipe(
        first()
      )
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

  /**
   * Categorizes entries by event
   * @param tournamentId
   * @private
   */
  private loadTournamentEvents(tournamentId: number) {
    // this selector will NOT be subscribed by template.
    // It exists only to first check if the events were loaded already and if they were it will stop,
    // if not it will initiate a load and then replace above observable with a new one.
    const localTournamentEvents$ = this.tournamentEventConfigService.store.select(
      this.tournamentEventConfigService.selectors.selectEntities);
    const subscription = localTournamentEvents$
      .pipe(
        repeat(),
        filter((events: TournamentEvent[]) => {
          let sameTournament: boolean = true;
          if (events != null && events.length > 0) {
            const firstEvent = events[0];
            sameTournament = (firstEvent.tournamentFk === tournamentId);
          } else {
            sameTournament = false;
          }
          if (!sameTournament) {
            // don't have event configs cached - load them
            this.tournamentEventConfigService.loadTournamentEvents(tournamentId);
          }
          return sameTournament;
        }),
        take(1)
      )
      .subscribe(
        (events: TournamentEvent[]) => {
          if (events != null && events.length > 0) {
            this.tournamentEvents$ = of(events);
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
      // console.log('Tournament start date PASSED from previous screen');
      const tournamentStartDate = new DateUtils().convertFromString(strTournamentStartDate);
      this.tournamentStartDate$ = of(tournamentStartDate);
    } else {
      const currentUser = this.authenticationService.getCurrentUser();
      if (currentUser == null) {
        this.getPublicTournamentInfo(tournamentId);
      } else {
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
                // console.log('got tournamentInfo from cache for START_DATE');
                const tournamentStartDate2 = new DateUtils().convertFromString(tournamentInfo.startDate);
                this.tournamentStartDate$ = of(tournamentStartDate2);
              } else {
                // console.log('tournamentInfo not in cache. getting from SERVER');
                // not in cache so get it. Since it is an entity collection it will be
                // piped to the above selector and processed by if branch
                this.tournamentInfoService.getByKey(tournamentId);
              }
            });
        this.subscriptions.add(subscription);
      }
    }
  }

  private getPublicTournamentInfo(tournamentId: number) {
    const url = `/publicapi/tournamentinfo/${tournamentId}`;
    this.httpClient.get(url).pipe(first())
      .subscribe((tournamentInfo: TournamentInfo) => {
      if (tournamentInfo != null) {
        const tournamentStartDate2 = new DateUtils().convertFromString(tournamentInfo.startDate);
        this.tournamentStartDate$ = of(tournamentStartDate2);
      }
    });
  }

}
