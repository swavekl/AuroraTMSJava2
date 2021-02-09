import {Component, OnDestroy, OnInit} from '@angular/core';
import {TournamentEntry} from '../model/tournament-entry.model';
import {combineLatest, Observable, of, pipe, Subscription} from 'rxjs';
import {TournamentEntryService} from '../service/tournament-entry.service';
import {ActivatedRoute, Router} from '@angular/router';
import {createSelector} from '@ngrx/store';
import {TournamentInfoService} from '../../tournament/tournament-info.service';
import {TournamentInfo} from '../../tournament/tournament-info.model';
import {TournamentEventConfigService} from '../../tournament-config/tournament-event-config.service';
import {TournamentEventEntryService} from '../service/tournament-event-entry.service';
import {TournamentEventEntry} from '../model/tournament-event-entry.model';
import {AuthenticationService} from '../../../user/authentication.service';
import {TournamentEvent} from '../../tournament-config/tournament-event.model';
import {TournamentEventEntryInfo} from '../model/tournament-event-entry-info-model';
import {DateUtils} from '../../../shared/date-utils';
import {TournamentEventEntryInfoService} from '../service/tournament-event-entry-info.service';
import {first, map, take} from 'rxjs/operators';

@Component({
  selector: 'app-entry-wizard-container',
  template: `
    <app-linear-progress-bar [loading]="loading$ | async"></app-linear-progress-bar>
    <app-entry-wizard [entry]="entry$ | async"
                      [teamsTournament]="teamsTournament$ | async"
                      [tournamentStartDate]="tournamentStartDate$ | async"
                      [allEventEntryInfos]="allEventEntryInfos$ | async"
                      [otherPlayers]="otherPlayers$ | async"
                      (tournamentEntryChanged)="onTournamentEntryChanged($event)"
                      (eventEntryChanged)="onEventEntryChanged($event)">
    </app-entry-wizard>
  `,
  styles: []
})
export class EntryWizardContainerComponent implements OnInit, OnDestroy {

  private entryId: number;
  entry$: Observable<TournamentEntry>;
  loading$: Observable<boolean>;

  teamsTournament$: Observable<boolean>;
  otherPlayers$: Observable<any>;
  tournamentEvents$: Observable<TournamentEvent[]>;
  tournamentEventEntries$: Observable<TournamentEventEntry[]>;
  // events and their status (confirmed, waiting list, not available etc.)
  allEventEntryInfos$: Observable<TournamentEventEntryInfo[]>;

  // tournament start date
  tournamentStartDate$: Observable<Date>;

  tournamentInfo: TournamentInfo;

  private subscriptions: Subscription = new Subscription();

  constructor(private tournamentEntryService: TournamentEntryService,
              private tournamentEventEntryService: TournamentEventEntryService,
              private tournamentInfoService: TournamentInfoService,
              private tournamentEventConfigService: TournamentEventConfigService,
              private tournamentEventEntryInfoService: TournamentEventEntryInfoService,
              private activatedRoute: ActivatedRoute,
              private router: Router,
              private authService: AuthenticationService) {

    // if any of the service are loading show the loading progress
    this.loading$ = combineLatest(
      this.tournamentEventEntryService.store.select(this.tournamentEventEntryService.selectors.selectLoading),
      this.tournamentEventEntryInfoService.store.select(this.tournamentEventEntryInfoService.selectors.selectLoading),
      this.tournamentEntryService.store.select(this.tournamentEntryService.selectors.selectLoading),
      (eventEntriesLoading: boolean, entryInfosLoading: boolean, entryLoading: boolean) => {
        // console.log ('loading selector = ' + eventEntriesLoading + ' || ' + entryInfosLoading + ' || ' + entryLoading);
        return eventEntriesLoading || entryInfosLoading || entryLoading;
      }
    );

    this.otherPlayers$ = of([
      {firstName: 'Mario', lastName: 'Lorenc', profileId: 2, entryId: 11},
      {firstName: 'Justine', lastName: 'Lorenc', profileId: 3, entryId: null},
      {firstName: 'Danielle', lastName: 'Lorenc', profileId: 4, entryId: null},
    ]);
  }

  ngOnInit(): void {
    const routePath = this.activatedRoute.snapshot.routeConfig.path;
    const creating: boolean = (routePath.endsWith('create'));

    const entryId = this.activatedRoute.snapshot.params['entryId'] || 0;
    const tournamentId = this.activatedRoute.snapshot.params['tournamentId'] || 0;

    this.selectEntry(entryId, creating, tournamentId);
    this.selectTournamentInfo(tournamentId);
  }

  /**
   *
   * @param entryId
   * @param creating
   * @param tournamentId
   */
  private selectEntry(entryId, creating: boolean, tournamentId) {
    // console.log('creating', creating);
    // console.log('entryId', entryId);
    if (creating) {
      const entryToEdit = new TournamentEntry();
      entryToEdit.tournamentFk = tournamentId;
      entryToEdit.type = 0; // EntryType.INDIVIDUAL
      entryToEdit.dateEntered = new Date();
      entryToEdit.profileId = this.authService.getCurrentUserProfileId();
      // create new entry
      this.entry$ = this.tournamentEntryService.upsert(entryToEdit);
      this.entry$.subscribe((tournamentEntry: TournamentEntry) => {
        // console.log('created new tournament entry', tournamentEntry);
        this.selectEventEntries(tournamentId, tournamentEntry.id);
        return tournamentEntry;
      });
    } else {
      // see if entry is cached on the client already
      // construct a selector to pick this one entry from cache
      const entityMapSelector = this.tournamentEntryService.selectors.selectEntityMap;
      const selectedEntrySelector = createSelector(
        entityMapSelector,
        (entityMap) => {
          return entityMap[entryId];
        });
      const selectedEntry$: Observable<TournamentEntry> = this.tournamentEntryService.store.select(selectedEntrySelector);
      const subscription = selectedEntry$.subscribe((next: TournamentEntry) => {
        console.log('got tournament entry', next);
        // editing - check if we had it in cache if not - then fetch it
        if (!next) {
          this.entry$ = this.tournamentEntryService.getByKey(entryId);
        } else {
          this.entry$ = of(next);
        }
        this.selectEventEntries(tournamentId, entryId);
      });
      this.subscriptions.add(subscription);
    }
  }

  /**
   *
   * @param tournamentId
   */
  selectTournamentInfo(tournamentId: number): void {
    // console.log ('tournamentId', tournamentId);
    const tournamentInfoSelector = this.tournamentInfoService.selectors.selectEntityMap;
    const selectedTournamentSelector = createSelector(
      tournamentInfoSelector,
      (entityMap) => {
        return entityMap[tournamentId];
      });
    const selectedTournamentInfo$: Observable<TournamentInfo> =
      this.tournamentInfoService.store.select(selectedTournamentSelector);
    const subscription = selectedTournamentInfo$.subscribe((tournamentInfo: TournamentInfo) => {
      if (tournamentInfo) {
        this.initTournamentData(tournamentInfo);
      } else {
        const subscription1 = this.tournamentInfoService.getByKey(tournamentId)
          .subscribe((tournamentInfo1: TournamentInfo) => {
            this.initTournamentData(tournamentInfo1);
        });
        this.subscriptions.add(subscription1);
      }
    });
    this.subscriptions.add(subscription);
  }

  private initTournamentData (tournamentInfo: TournamentInfo): void {
    this.tournamentInfo = tournamentInfo;
    const isTeamsTournament: boolean = (tournamentInfo) ? (tournamentInfo?.tournamentType === 'Teams') : false;
    // console.log('isTeamsTournament', isTeamsTournament);
    this.teamsTournament$ = of(isTeamsTournament);
    const startDate = new DateUtils().convertFromString(tournamentInfo.startDate);
    this.tournamentStartDate$ = of(startDate);
  }

  ngOnDestroy(): void {
    if (this.subscriptions) {
      this.subscriptions.unsubscribe();
    }
  }

  private selectEventEntries(tournamentId: number, entryId: number) {

    this.entryId = entryId;

    // // request tournament events
    // this.tournamentEvents$ = this.tournamentEventConfigService.entities$;
    // this.tournamentEventConfigService.loadTournamentEvents(tournamentId);
    //
    // request event entries
    this.tournamentEventEntries$ = this.tournamentEventEntryService.entities$;
    this.tournamentEventEntryService.getAllForTournamentEntry(entryId);
    //
    // // setup combining selector
    // const combinedSelector = combineLatest(this.tournamentEvents$, this.tournamentEventEntries$);
    // const subscription5 = combinedSelector.subscribe(([tournamentEvents, tournamentEventEntries]) => {
    //   // console.log('tournament events', tournamentEvents);
    //   // console.log('tournament event entries', tournamentEventEntries);
    //   const allEventEntries: any [] = this.determineEvents(tournamentEvents, tournamentEventEntries);
    //   // Observable<[ObservedValueOf<O1>, ObservedValueOf<O2>]>
    //   this.allEventEntryInfos$ = of(allEventEntries);
    // });
    // this.subscriptions.add(subscription5);

    this.allEventEntryInfos$ = this.tournamentEventEntryInfoService.entities$;
    this.tournamentEventEntryInfoService.loadForTournamentEntry(this.entryId);
  }

  /**
   * combines event information with event entries
   * @param tournamentEvents
   * @param tournamentEventEntries
   * @private
   */
  private determineEvents(tournamentEvents: TournamentEvent [],
                          tournamentEventEntries: TournamentEventEntry []): TournamentEventEntryInfo [] {

    const allEventEntries: TournamentEventEntryInfo [] = [];
    for (let i = 0; i < tournamentEvents.length; i++) {
      const event = tournamentEvents[i];
      let entered = false;
      for (let j = 0; j < tournamentEventEntries.length; j++) {
        const eventEntry = tournamentEventEntries[j];
        if (eventEntry.tournamentEventFk === event.id) {
          entered = true;
          const eventEntryInfo: TournamentEventEntryInfo = {
            id: i,
            event: event,
            eventEntry: eventEntry
          };
          allEventEntries.push(eventEntryInfo);
          break;
        }
      }
      if (!entered) {
        const eventEntryInfo: TournamentEventEntryInfo = {
          id: i,
          event: event,
          eventEntry: new TournamentEventEntry()
        };
        allEventEntries.push(eventEntryInfo);
      }
    }
    // console.log('allEventEntries ', allEventEntries);
    return allEventEntries;
  }

  onEventEntryChanged(tournamentEventEntry: TournamentEventEntry) {
    console.log ('onEventEntryChanged tournamentEventEntry', tournamentEventEntry);
    const subscription = this.tournamentEventEntryService.upsert(tournamentEventEntry)
      .pipe(first())
      .subscribe(
        (value: TournamentEventEntry) => {
          console.log ('successfully changed event entry.  Loading entry infos again...');
          this.tournamentEventEntryInfoService.loadForTournamentEntry(this.entryId);
        },
        (errorObj: any) => {
          console.log ('Unable to enter this event up');
        },
        () => {
          console.log ('completed');
        }
      );
    this.subscriptions.add(subscription);
  }

  onTournamentEntryChanged(tournamentEntry: TournamentEntry) {
    this.tournamentEntryService.upsert(tournamentEntry);
  }
}
