import {Component, OnDestroy, OnInit} from '@angular/core';
import {TournamentEntry} from '../model/tournament-entry.model';
import {combineLatest, Observable, of, Subscription} from 'rxjs';
import {TournamentEntryService} from '../service/tournament-entry.service';
import {ActivatedRoute, Router} from '@angular/router';
import {createSelector} from '@ngrx/store';
import {TournamentInfoService} from '../../tournament/tournament-info.service';
import {TournamentInfo} from '../../tournament/tournament-info.model';
import {TournamentEventConfigService} from '../../tournament-config/tournament-event-config.service';
import {TournamentEventEntryService} from '../service/tournament-event-entry.service';
import {TournamentEventEntry} from '../model/tournament-event-entry.model';
import {TournamentEvent} from '../../tournament-config/tournament-event.model';
import {TournamentEventEntryInfo} from '../model/tournament-event-entry-info-model';
import {DateUtils} from '../../../shared/date-utils';
import {TournamentEventEntryInfoService} from '../service/tournament-event-entry-info.service';
import {first} from 'rxjs/operators';
import {LinearProgressBarService} from '../../../shared/linear-progress-bar/linear-progress-bar.service';

@Component({
  selector: 'app-entry-wizard-container',
  template: `
    <app-entry-wizard [entry]="entry$ | async"
                      [teamsTournament]="teamsTournament$ | async"
                      [tournamentStartDate]="tournamentStartDate$ | async"
                      [allEventEntryInfos]="allEventEntryInfos$ | async"
                      [otherPlayers]="otherPlayers$ | async"
                      (tournamentEntryChanged)="onTournamentEntryChanged($event)"
                      (eventEntryChanged)="onEventEntryChanged($event)"
                      (finish)="onFinish($event)">
    </app-entry-wizard>
  `,
  styles: []
})
export class EntryWizardContainerComponent implements OnInit, OnDestroy {

  private entryId: number;
  private tournamentId: number;
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
              private linearProgressBarService: LinearProgressBarService) {

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

    const subscription = this.loading$.subscribe((loading: boolean) => {
      console.log ('combined subscription', loading);
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(subscription);

    this.otherPlayers$ = of([
      {firstName: 'Mario', lastName: 'Lorenc', profileId: 2, entryId: 11},
      {firstName: 'Justine', lastName: 'Lorenc', profileId: 3, entryId: null},
      {firstName: 'Danielle', lastName: 'Lorenc', profileId: 4, entryId: null},
    ]);
  }

  ngOnInit(): void {
    this.entryId = this.activatedRoute.snapshot.params['entryId'] || 0;
    this.tournamentId = this.activatedRoute.snapshot.params['tournamentId'] || 0;

    this.selectTournamentInfo(this.tournamentId);
    this.selectEntry(this.entryId);
    this.selectEventEntries(this.entryId);
  }

  /**
   * Destroys all subscriptions
   */
  ngOnDestroy(): void {
    console.log ('in ngOnDestroy');
    if (this.subscriptions != null) {
      console.log ('in ngOnDestroy - unsubscribing');
      this.subscriptions.unsubscribe();
    }
  }

  /**
   * Gets tournament entry
   * @param entryId entry id
   */
  private selectEntry(entryId: number) {
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
    });
    this.subscriptions.add(subscription);
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
    // tournament information will not change just get it once
    this.tournamentInfoService.store.select(selectedTournamentSelector)
      .pipe(first())
      .subscribe((tournamentInfo: TournamentInfo) => {
        console.log('got tournament info', tournamentInfo);
        if (tournamentInfo) {
          this.initTournamentData(tournamentInfo);
        } else {
          console.log ('tournament info not in cache ??');
          this.tournamentInfoService.getByKey(tournamentId)
            .pipe(first())
            .subscribe((tournamentInfo1: TournamentInfo) => {
              console.log('got tournament info', tournamentInfo1);
              this.initTournamentData(tournamentInfo1);
            });
        }
      });
  }

  private initTournamentData(tournamentInfo: TournamentInfo): void {
    this.tournamentInfo = tournamentInfo;
    const isTeamsTournament: boolean = (tournamentInfo) ? (tournamentInfo?.tournamentType === 'Teams') : false;
    // console.log('isTeamsTournament', isTeamsTournament);
    this.teamsTournament$ = of(isTeamsTournament);
    const startDate = new DateUtils().convertFromString(tournamentInfo.startDate);
    this.tournamentStartDate$ = of(startDate);
  }

  /**
   * Selects event entries associated with this tournament entry
   * @param entryId entry id
   * @private
   */
  private selectEventEntries(entryId: number) {

    this.entryId = entryId;

    // infos combine event and event entry which contains status (entered, waiting list, time conflict etc.)
    this.allEventEntryInfos$ = this.tournamentEventEntryInfoService.entities$;
    // subscribe to event entry changes so when they are loaded or
    // are updated as a result of enter/withdraw we can recalculate the event eligibility
    this.tournamentEventEntries$ = this.tournamentEventEntryService.entities$;
    const subscription: Subscription = this.tournamentEventEntries$.subscribe(
      (value: TournamentEventEntry[]) => {
        console.log('Loaded event entries - recalculating event eligibility');
        this.tournamentEventEntryInfoService.loadForTournamentEntry(this.entryId);
      },
      (error: any) => {
        console.log('Unable to recalculate events eligibility', error);
      },
      () => {
        // console.log ('completed');
      }
    );
    this.subscriptions.add(subscription);

    // initiate the call to load event entries
    this.tournamentEventEntryService.loadAllForTournamentEntry(entryId);
  }

  /**
   * Updates or inserts tournament event entry
   * @param tournamentEventEntry
   */
  onEventEntryChanged(tournamentEventEntry: TournamentEventEntry) {
    console.log('onEventEntryChanged tournamentEventEntry', tournamentEventEntry);
    // after update the event eligibility will be refetched by subscription to event entry changes
    this.tournamentEventEntryService.upsert(tournamentEventEntry);
  }

  /**
   * Updates tournament entry
   * @param tournamentEntry
   */
  onTournamentEntryChanged(tournamentEntry: TournamentEntry) {
    this.tournamentEntryService.update(tournamentEntry)
      .pipe(first())
      .subscribe(
        (value: TournamentEntry) => {
          console.log('updated successfully tournament entry');
        },
        (error: any) => {
          console.log('error updating entry', error);
        },
        () => {
          // console.log ('completed');
        }
      );
  }

  onFinish(event: any) {
    this.router.navigateByUrl(`/tournaments/view/${this.tournamentId}`);
  }
}
