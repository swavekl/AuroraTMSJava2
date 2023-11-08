import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {LinearProgressBarService} from '../../../shared/linear-progress-bar/linear-progress-bar.service';
import {combineLatest, Observable, of, Subscription} from 'rxjs';
import {TournamentEntry} from '../model/tournament-entry.model';
import {TournamentEntryService} from '../service/tournament-entry.service';
import {TournamentConfigService} from '../../tournament-config/tournament-config.service';
import {TournamentEventConfigService} from '../../tournament-config/tournament-event-config.service';
import {EventEntryInfoService} from '../service/event-entry-info.service';
import {createSelector} from '@ngrx/store';
import {Tournament} from '../../tournament-config/tournament.model';
import {TournamentEventEntryInfo} from '../model/tournament-event-entry-info-model';
import {TournamentEvent} from '../../tournament-config/tournament-event.model';
import {first} from 'rxjs/operators';
import {Profile} from '../../../profile/profile';
import {AuthenticationService} from '../../../user/authentication.service';
import {ProfileService} from '../../../profile/profile.service';
import {PaymentRefundFor} from '../../../account/model/payment-refund-for.enum';
import {PaymentRefund} from '../../../account/model/payment-refund.model';
import {PaymentRefundService} from '../../../account/service/payment-refund.service';

@Component({
  selector: 'app-entry-view-container',
  template: `
    <app-entry-view [entry]="entry$ | async"
                    [tournament]="tournament$ | async"
                    [allEventEntryInfos]="allEventEntryInfos$ | async"
                    [playerProfile]="playerProfile$ | async"
                    [paymentsRefunds]="paymentsRefunds$ | async"
                    [canChangeRating]="canChangeRating"
                    (action)="onAction($event)"
                    (updateEntry)="onUpdateTournamentEntry($event)"
    >
    </app-entry-view>
  `,
  styles: []
})
export class EntryViewContainerComponent implements OnInit, OnDestroy {

  private entryId: number;
  private tournamentId: number;
  entry$: Observable<TournamentEntry>;
  loading$: Observable<boolean>;

  // tournament configuration information
  tournament$: Observable<Tournament>;

  // events and their status (confirmed, waiting list, not available etc.)
  allEventEntryInfos$: Observable<TournamentEventEntryInfo[]>;

  // player profile needed for summary
  playerProfile$: Observable<Profile>;

  paymentsRefunds$: Observable<PaymentRefund[]>;

  private returnUrl: string = null;

  canChangeRating: boolean = false;

  private subscriptions: Subscription = new Subscription();

  constructor(private tournamentEntryService: TournamentEntryService,
              private tournamentConfigService: TournamentConfigService,
              private tournamentEventConfigService: TournamentEventConfigService,
              private eventEntryInfoService: EventEntryInfoService,
              private authenticationService: AuthenticationService,
              private paymentRefundService: PaymentRefundService,
              private profileService: ProfileService,
              private activatedRoute: ActivatedRoute,
              private router: Router,
              private linearProgressBarService: LinearProgressBarService) {

    this.setupProgressIndicator();

  }

  private setupProgressIndicator() {
    this.loading$ = combineLatest([
        this.eventEntryInfoService.loading$,
        this.tournamentConfigService.store.select(this.tournamentConfigService.selectors.selectLoading),
        this.tournamentEntryService.store.select(this.tournamentEntryService.selectors.selectLoading),
        this.tournamentEventConfigService.store.select(this.tournamentEventConfigService.selectors.selectLoading),
        this.profileService.loading$,
      this.paymentRefundService.loading$,
      ], (eventEntryInfosLoading: boolean, tournamentLoading: boolean, entryLoading: boolean, eventConfigLoading: boolean, profileLoading: boolean, paymentsRefundsLoading: boolean) => {
        return eventEntryInfosLoading || tournamentLoading || entryLoading || eventConfigLoading || profileLoading || paymentsRefundsLoading;
      }
    );

    const subscription = this.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(subscription);
  }

  ngOnDestroy(): void {
    if (this.subscriptions != null) {
      this.subscriptions.unsubscribe();
    }
  }

  ngOnInit(): void {
    this.entryId = this.activatedRoute.snapshot.params['entryId'] || 0;
    this.tournamentId = this.activatedRoute.snapshot.params['tournamentId'] || 0;
    this.selectTournament(this.tournamentId);
    this.selectEntry(this.entryId);
    this.loadEventEntriesInfos(this.entryId);
    this.loadPaymentRefunds(this.entryId);
    this.returnUrl = history?.state?.returnUrl;
    this.canChangeRating = history?.state?.canChangeRating || false;
  }

  /**
   * Gets tournament configuration
   * @param tournamentId
   * @private
   */
  private selectTournament(tournamentId: number) {
    // console.log('selecting tournament config for tournament ' + tournamentId);
    const selectedTournamentSelector = createSelector(
      this.tournamentConfigService.selectors.selectEntityMap,
      (entityMap) => {
        return entityMap[tournamentId];
      });
    // tournament information will not change just get it once
    this.tournament$ = this.tournamentConfigService.store.select(selectedTournamentSelector);
    const subscription = this.tournament$.subscribe((tournament: Tournament) => {
      // console.log('got tournament is null ', (tournament == null));
      if (!tournament) {
        // console.log('tournament not in cache get from SERVER');
        this.tournamentConfigService.getByKey(tournamentId);
      } else {
        // console.log ('full tournament config ' + JSON.stringify(tournament));
      }
    });
    this.subscriptions.add(subscription);
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
    this.entry$ = this.tournamentEntryService.store.select(selectedEntrySelector);
    const subscription = this.entry$.subscribe((entry: TournamentEntry) => {
      // console.log('got tournament entry', next);
      // editing - check if we had it in cache if not - then fetch it
      if (!entry) {
        this.entry$ = this.tournamentEntryService.getByKey(entryId);
      } else {
        this.loadPlayerProfile(entry.profileId);
      }
    });
    this.subscriptions.add(subscription);
  }

  /**
   *
   * @param entryId
   * @private
   */
  private loadEventEntriesInfos(entryId: number) {
    this.entryId = entryId;

    // load them but not cache them, result is subscribed by async pipe
    this.allEventEntryInfos$ = combineLatest([
      this.eventEntryInfoService.eventEntryInfos$,
      this.tournamentEventConfigService.store.select(this.tournamentEventConfigService.selectors.selectEntities)],
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

  private loadPlayerProfile(playerProfileId: string) {
    // const playerProfileId = this.authenticationService.getCurrentUserProfileId();
    this.profileService.getProfile(playerProfileId)
      .pipe(first())
      .subscribe((profile: Profile) => {
        this.playerProfile$ = of(profile);
      });
  }

  private loadPaymentRefunds(tournamentEntryId: number) {
    // console.log('loading payments refunds for entry ' + tournamentEntryId);
    this.paymentRefundService.listPaymentsRefunds(PaymentRefundFor.TOURNAMENT_ENTRY, tournamentEntryId)
      .pipe(first())
      .subscribe(
        (paymentsRefunds: PaymentRefund[]) => {
          this.paymentsRefunds$ = of(paymentsRefunds);
        },
        (error: any) => {
          console.log('error getting payment refunds' + JSON.stringify(error));
        }
      );
  }

  onAction(action: string) {
    if (action === 'modify') {
      const url = `ui/entries/entrywizard/${this.tournamentId}/edit/${this.entryId}`;
      this.router.navigateByUrl(url);
    } else if (action === 'withdraw') {
      const url = `ui/entries/entrywizard/${this.tournamentId}/edit/${this.entryId}?withdraw=true`;
      this.router.navigateByUrl(url);
    } else if (action === 'back') {
      const url = (this.returnUrl == null) ? `ui/tournaments/view/${this.tournamentId}` : this.returnUrl;
      this.router.navigateByUrl(url);
    }
  }

  onUpdateTournamentEntry(tournamentEntry: TournamentEntry) {
    this.tournamentEntryService.update(tournamentEntry)
      .pipe(first())
      .subscribe(
        (updatedTournamentEntry: TournamentEntry) => {
          // console.log('updated successfully tournament entry', updatedTournamentEntry);
          this.entry$ = of(updatedTournamentEntry);
        },
        (error: any) => {
          console.log('error updating entry', error);
        },
        () => {
          // console.log ('completed');
        }
      );
  }
}
