import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {combineLatest, Observable, of, Subscription} from 'rxjs';
import {first} from 'rxjs/operators';
import {createSelector} from '@ngrx/store';
import {TournamentEntry} from '../model/tournament-entry.model';
import {TournamentEntryService} from '../service/tournament-entry.service';
import {TournamentInfo} from '../../tournament/tournament-info.model';
import {TournamentInfoService} from '../../tournament/tournament-info.service';
import {TournamentEvent} from '../../tournament-config/tournament-event.model';
import {TournamentEventConfigService} from '../../tournament-config/tournament-event-config.service';
import {TournamentEventEntryInfo} from '../model/tournament-event-entry-info-model';
import {EventEntryInfoService} from '../service/event-entry-info.service';
import {DateUtils} from '../../../shared/date-utils';
import {LinearProgressBarService} from '../../../shared/linear-progress-bar/linear-progress-bar.service';
import {Profile} from '../../../profile/profile';
import {AuthenticationService} from '../../../user/authentication.service';
import {ProfileService} from '../../../profile/profile.service';

@Component({
  selector: 'app-entry-wizard-container',
  template: `
    <app-entry-wizard [entry]="entry$ | async"
                      [teamsTournament]="teamsTournament$ | async"
                      [tournamentStartDate]="tournamentStartDate$ | async"
                      [allEventEntryInfos]="allEventEntryInfos$ | async"
                      [playerProfile]="playerProfile$ | async"
                      [otherPlayers]="otherPlayers$ | async"
                      (tournamentEntryChanged)="onTournamentEntryChanged($event)"
                      (confirmEntries)="onConfirmEntries($event)"
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
  playerProfile$: Observable<Profile>;
  otherPlayers$: Observable<any>;
  // events and their status (confirmed, waiting list, not available etc.)
  allEventEntryInfos$: Observable<TournamentEventEntryInfo[]>;

  // tournament start date
  tournamentStartDate$: Observable<Date>;

  tournamentInfo: TournamentInfo;

  private subscriptions: Subscription = new Subscription();

  constructor(private tournamentEntryService: TournamentEntryService,
              private tournamentInfoService: TournamentInfoService,
              private tournamentEventConfigService: TournamentEventConfigService,
              private eventEntryInfoService: EventEntryInfoService,
              private activatedRoute: ActivatedRoute,
              private router: Router,
              private linearProgressBarService: LinearProgressBarService,
              private authenticationService: AuthenticationService,
              private profileService: ProfileService) {

    // if any of the service are loading show the loading progress
    this.loading$ = combineLatest(
      eventEntryInfoService.loading$,
      this.tournamentEntryService.store.select(this.tournamentEntryService.selectors.selectLoading),
      this.tournamentEventConfigService.store.select(this.tournamentEventConfigService.selectors.selectLoading),
      (eventEntryInfosLoading: boolean, entryLoading: boolean, eventConfigLoading: boolean) => {
        return eventEntryInfosLoading || entryLoading || eventConfigLoading;
      }
    );

    const subscription = this.loading$.subscribe((loading: boolean) => {
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
    this.loadEventEntriesInfos(this.entryId);
    this.loadPlayerProfile();
  }

  /**
   * Destroys all subscriptions
   */
  ngOnDestroy(): void {
    if (this.subscriptions != null) {
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
      // console.log('got tournament entry', next);
      // editing - check if we had it in cache if not - then fetch it
      if (!next) {
        this.entry$ = this.tournamentEntryService.getByKey(entryId);
      } else {
        this.entry$ = of(next);
      }
    });
    this.subscriptions.add(subscription);
  }

  private selectOtherEntries(owningEntryId: number) {

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
        // console.log('got tournament info', tournamentInfo);
        if (tournamentInfo) {
          this.initTournamentData(tournamentInfo);
        } else {
          // console.log('tournament info not in cache ??');
          this.tournamentInfoService.getByKey(tournamentId)
            .pipe(first())
            .subscribe((tournamentInfo1: TournamentInfo) => {
              // console.log('got tournament info', tournamentInfo1);
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
   *
   * @param entryId
   * @private
   */
  private loadEventEntriesInfos(entryId: number) {
    this.entryId = entryId;

    // load them but not cache them, result is subscribed by async pipe
    this.allEventEntryInfos$ = combineLatest(
      this.eventEntryInfoService.eventEntryInfos$,
      this.tournamentEventConfigService.store.select(this.tournamentEventConfigService.selectors.selectEntities),
      (eventEntryInfos: TournamentEventEntryInfo[], events: TournamentEvent[]) => {
        // console.log('got event infos and events, combining ...');
        // combine event information into event entry info
        eventEntryInfos.forEach((eventEntryInfo: TournamentEventEntryInfo) => {
          events.forEach((event: TournamentEvent) => {
            if (eventEntryInfo.eventFk === event.id) {
              eventEntryInfo.event = event;
              // console.log('combined event with event info for event ', eventEntryInfo);
            }
          });
        });
        return eventEntryInfos;
      }
    );
    // initiate the call to load events - this will be cached
    this.tournamentEventConfigService.loadTournamentEvents(this.tournamentId);

    // initiate call to get event entry infos with status
    this.eventEntryInfoService.getEventEntryInfos(this.entryId);
  }

  /**
   * Updates or inserts tournament event entry
   * @param tournamentEventEntryInfo
   */
  onEventEntryChanged(tournamentEventEntryInfo: TournamentEventEntryInfo) {
    // console.log('onEventEntryChanged tournamentEventEntryInfo', tournamentEventEntryInfo);
    this.eventEntryInfoService.changeEntryStatus(this.entryId, tournamentEventEntryInfo)
      .subscribe((success: boolean) => {
        // reload them after
        this.eventEntryInfoService.getEventEntryInfos(this.entryId);
      });
  }

  /**
   *
   * @param tournamentEntry
   */
  onConfirmEntries(tournamentEntry: TournamentEntry): void {
    if (tournamentEntry) {
      this.eventEntryInfoService.confirmEntries(this.entryId)
        .pipe(first())
        .subscribe((success: boolean) => {
          console.log('confirmed all - success', success);
          // reload them after
          this.eventEntryInfoService.getEventEntryInfos(this.entryId);
        });
    }
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

  private loadPlayerProfile() {
    const playerProfileId = this.authenticationService.getCurrentUserProfileId();
    this.profileService.getProfile(playerProfileId)
      .pipe(first())
      .subscribe((profile: Profile) => {
        this.playerProfile$ = of(profile);
      });

  }
}
