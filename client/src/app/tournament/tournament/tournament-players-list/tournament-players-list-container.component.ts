import {Component, OnDestroy, OnInit} from '@angular/core';
import {TournamentEntryInfoService} from '../../service/tournament-entry-info.service';
import {ActivatedRoute} from '@angular/router';
import {BehaviorSubject, combineLatest, Observable, of, Subscription, from} from 'rxjs';
import {distinctUntilChanged, filter, first, map, repeat, take, concatMap, delay, bufferCount, scan} from 'rxjs/operators';
import {LinearProgressBarService} from '../../../shared/linear-progress-bar/linear-progress-bar.service';
import {TournamentEntryInfo} from '../../model/tournament-entry-info.model';
import {TournamentEvent} from '../../tournament-config/tournament-event.model';
import {TournamentEventConfigService} from '../../tournament-config/tournament-event-config.service';
import {createSelector} from '@ngrx/store';
import {TournamentInfo} from '../../model/tournament-info.model';
import {TournamentInfoService} from '../../service/tournament-info.service';
import {DateUtils} from '../../../shared/date-utils';
import {AuthenticationService} from '../../../user/authentication.service';
import {HttpClient} from '@angular/common/http';
import {EventEntryType} from '../../tournament-config/model/event-entry-type.enum';
import {TeamService} from '../../tournament-entry/service/team.service';
import {Team} from '../../tournament-entry/model/team.model';

@Component({
    selector: 'app-tournament-player-list-container',
    template: `
    <app-tournament-players-list [entryInfos]="entryInfos$ | async"
                                 [tournamentEvents]="tournamentEvents$ | async"
                                 [tournamentStartDate]="tournamentStartDate$ | async"
                                 [tournamentEndDate]="tournamentEndDate$ | async"
                                 [tournamentName]="tournamentName$ | async"
                                 [teams]="teams$ | async"
    ></app-tournament-players-list>
  `,
    styles: [],
    standalone: false
})
export class TournamentPlayersListContainerComponent implements OnInit, OnDestroy {

  private subscriptions: Subscription = new Subscription();

  entryInfos$: Observable<TournamentEntryInfo[]>;
  tournamentEvents$: Observable<TournamentEvent[]>;
  tournamentStartDate$: Observable<Date>;
  tournamentEndDate$: Observable<Date>;
  tournamentName$: Observable<string>;
  teams$: BehaviorSubject<Team[]> = new BehaviorSubject<Team[]>([]);

  loading$: Observable<boolean>;

  constructor(private tournamentEntryInfoService: TournamentEntryInfoService,
              private tournamentInfoService: TournamentInfoService,
              private tournamentEventConfigService: TournamentEventConfigService,
              private activatedRoute: ActivatedRoute,
              private linearProgressBarService: LinearProgressBarService,
              private authenticationService: AuthenticationService,
              private httpClient: HttpClient,
              private teamService: TeamService) {
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
    this.loadTeamsIfNecessary(tournamentId)
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  /**
   * Loads tournament entries with full information.
   * The entries are grouped into chunks of 50 to reduce the number of HTTP requests.
   * The chunks are painted one after another to reduce the perceived delay between chunks.
   * The chunks are re-assembled into a single array for the UI to consume.
   * @param tournamentId tournament id
   * @private
   */
  private loadTournamentEntries(tournamentId: number) {
  const CHUNK_SIZE = 50;
  const DELAY_MS = 100;

  this.entryInfos$ = this.tournamentEntryInfoService.getAll(tournamentId).pipe(
    first(),
    // 1. Convert the large array into a stream of individual items
    concatMap(infos => from(infos)),

    // 2. Group items into chunks of 50
    bufferCount(CHUNK_SIZE),

    // 3. Space the chunks out so the browser can paint between them
    concatMap((chunk, index) => {
        return of(chunk).pipe(delay(index === 0 ? 0 : DELAY_MS));
      }
    ),

    // 4. Re-accumulate the chunks into a single growing array for the UI
    scan((acc: TournamentEntryInfo[], chunk: TournamentEntryInfo[]) => [...acc, ...chunk], [])
  );
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
      const strTournamentEndDate = history?.state?.tournamentEndDate;
      if (strTournamentEndDate != null) {
        const tournamentEndDate = new DateUtils().convertFromString(strTournamentEndDate);
        this.tournamentEndDate$ = of(tournamentEndDate);
      }
      this.tournamentName$ = of(history?.state?.tournamentName);
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
                const tournamentEndDate2 = new DateUtils().convertFromString(tournamentInfo.endDate);
                this.tournamentEndDate$ = of(tournamentEndDate2);
                this.tournamentName$ = of(tournamentInfo.name);
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
        const tournamentEndDate2 = new DateUtils().convertFromString(tournamentInfo.endDate);
        this.tournamentEndDate$ = of(tournamentEndDate2);
        this.tournamentName$ = of(tournamentInfo.name);
      }
    });
  }

  /**
   * Monitors the event store and triggers team loading only if
   * TEAM type events are present in the current tournament.
   */
  private loadTeamsIfNecessary(tournamentId: number) {
    this.tournamentEventConfigService.store.select(
      this.tournamentEventConfigService.selectors.selectEntities
    ).pipe(
      // 1. Convert entities map to array
      map(entities => Object.values(entities)),
      // 2. Only proceed if we actually have events loaded
      filter(events => events && events.length > 0),
      // 3. Extract IDs of events that are designated as TEAM events
      map(events => events
        .filter(e => e.eventEntryType === EventEntryType.TEAM)
        .map(e => e.id)
      ),
      // 4. Ensure we don't trigger multiple times for the same set of IDs
      distinctUntilChanged((prev, curr) => JSON.stringify(prev) === JSON.stringify(curr)),
      // 5. Only move forward if the list of team events is NOT empty
      filter(teamEventIds => teamEventIds.length > 0),
      // 6. Ensure this logic only runs once per component lifecycle
      take(1)
    ).subscribe(teamEventIds => {
      console.log('Team events detected, fetching teams...', teamEventIds);
      this.fetchTeams(teamEventIds, tournamentId);
    });
  }
  /**
   * Purely responsible for the HTTP call and updating the local state.
   */
  private fetchTeams(teamEventIds: number[], tournamentId: number) {
    const strTeamEventIds = teamEventIds.join(",");
    const query = `teamEventIds=${strTeamEventIds}&tournamentId=${tournamentId}`;
    this.teamService.loadWithQuery(query)
      .subscribe({
        next: (teams) => {
          this.teams$.next(teams);
        },
        error: (err) => {
          console.error('Failed to load teams', err);
          this.teams$.next([]);
        }
      });
  }

}
