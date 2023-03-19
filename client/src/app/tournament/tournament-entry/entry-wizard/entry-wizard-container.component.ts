import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {combineLatest, Observable, of, Subscription} from 'rxjs';
import {first, map} from 'rxjs/operators';
import {createSelector} from '@ngrx/store';
import {TournamentEntry} from '../model/tournament-entry.model';
import {TournamentEntryService} from '../service/tournament-entry.service';
import {TournamentEvent} from '../../tournament-config/tournament-event.model';
import {TournamentEventConfigService} from '../../tournament-config/tournament-event-config.service';
import {TournamentEventEntryInfo} from '../model/tournament-event-entry-info-model';
import {EventEntryInfoService} from '../service/event-entry-info.service';
import {LinearProgressBarService} from '../../../shared/linear-progress-bar/linear-progress-bar.service';
import {Profile} from '../../../profile/profile';
import {AuthenticationService} from '../../../user/authentication.service';
import {ProfileService} from '../../../profile/profile.service';
import {PaymentRefund} from '../../../account/model/payment-refund.model';
import {PaymentRefundService} from '../../../account/service/payment-refund.service';
import {TournamentConfigService} from '../../tournament-config/tournament-config.service';
import {Tournament} from '../../tournament-config/tournament.model';
import {PaymentRefundFor} from '../../../account/model/payment-refund-for.enum';
import {EntryWizardComponent} from './entry-wizard.component';
import {CartSessionService} from '../../../account/service/cart-session.service';

@Component({
  selector: 'app-entry-wizard-container',
  template: `
    <app-entry-wizard [entry]="entry$ | async"
                      [tournament]="tournament$ | async"
                      [allEventEntryInfos]="allEventEntryInfos$ | async"
                      [playerProfile]="playerProfile$ | async"
                      [paymentsRefunds]="paymentsRefunds$ | async"
                      [isWithdrawing]="withdrawing"
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

  playerProfile$: Observable<Profile>;
  // events and their status (confirmed, waiting list, not available etc.)
  allEventEntryInfos$: Observable<TournamentEventEntryInfo[]>;

  // tournament configuration information
  tournament$: Observable<Tournament>;

  paymentsRefunds$: Observable<PaymentRefund[]>;

  // session id used for detecting abandonded cart
  cartSessionId: string;

  private subscriptions: Subscription = new Subscription();

  @ViewChild(EntryWizardComponent)
  private entryWizardComponent: EntryWizardComponent;

  withdrawing: boolean = false;

  constructor(private tournamentEntryService: TournamentEntryService,
              private tournamentConfigService: TournamentConfigService,
              private tournamentEventConfigService: TournamentEventConfigService,
              private eventEntryInfoService: EventEntryInfoService,
              private activatedRoute: ActivatedRoute,
              private router: Router,
              private linearProgressBarService: LinearProgressBarService,
              private authenticationService: AuthenticationService,
              private profileService: ProfileService,
              private paymentRefundService: PaymentRefundService,
              private cartSessionService: CartSessionService) {

    // if any of the service are loading show the loading progress
    this.loading$ = combineLatest(
      eventEntryInfoService.loading$,
      this.tournamentConfigService.store.select(this.tournamentConfigService.selectors.selectLoading),
      this.tournamentEntryService.store.select(this.tournamentEntryService.selectors.selectLoading),
      this.tournamentEventConfigService.store.select(this.tournamentEventConfigService.selectors.selectLoading),
      (eventEntryInfosLoading: boolean, tournamentLoading: boolean, entryLoading: boolean, eventConfigLoading: boolean) => {
        return eventEntryInfosLoading || tournamentLoading || entryLoading || eventConfigLoading;
      }
    );

    const subscription = this.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(subscription);
  }

  ngOnInit(): void {
    this.entryId = this.activatedRoute.snapshot.params['entryId'] || 0;
    this.tournamentId = this.activatedRoute.snapshot.params['tournamentId'] || 0;
    this.withdrawing = this.activatedRoute.snapshot.queryParamMap.get('withdraw') === 'true';
    this.selectTournament(this.tournamentId);
    this.selectEntry(this.entryId);
    this.loadEventEntriesInfos(this.entryId);
    this.loadPlayerProfile();
    this.loadPaymentRefunds(this.entryId);
    this.startCartSession();
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
    this.entry$ = this.tournamentEntryService.store.select(selectedEntrySelector);
    const subscription = this.entry$.subscribe((next: TournamentEntry) => {
      // console.log('got tournament entry', next);
      // editing - check if we had it in cache if not - then fetch it
      if (!next) {
        this.entry$ = this.tournamentEntryService.getByKey(entryId);
      }
    });
    this.subscriptions.add(subscription);
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
    tournamentEventEntryInfo.cartSessionId = this.cartSessionId;
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
      this.eventEntryInfoService.confirmEntries(this.entryId, this.cartSessionId, this.withdrawing)
        .pipe(first())
        .subscribe((success: boolean) => {
          console.log('confirmed all - success', success);
          // reload them after
          this.eventEntryInfoService.getEventEntryInfos(this.entryId);

          // reload payments refunds
          this.loadPaymentRefunds(this.entryId);
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
    this.router.navigateByUrl(`/ui/tournaments/view/${this.tournamentId}`);
  }

  private loadPlayerProfile() {
    const playerProfileId = this.authenticationService.getCurrentUserProfileId();
    this.profileService.getProfile(playerProfileId)
      .pipe(first())
      .subscribe((profile: Profile) => {
        this.playerProfile$ = of(profile);
      });

  }

  /**
   * Loads payments or refunds if any
   * @param tournamentEntryId
   * @private
   */
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

  public isDirty(): boolean {
    return this.entryWizardComponent.isDirty();
  }

  private startCartSession() {
    const subscription = this.cartSessionService.startSession(PaymentRefundFor.TOURNAMENT_ENTRY)
      .pipe(
        first(),
        map((cartSessionId: string) => {
          const now = new Date();
          // console.log(`Started cartSessionId ${cartSessionId} at ${now}`);
          this.cartSessionId = cartSessionId;
        })
      ).subscribe();
    this.subscriptions.add(subscription);
  }
}
