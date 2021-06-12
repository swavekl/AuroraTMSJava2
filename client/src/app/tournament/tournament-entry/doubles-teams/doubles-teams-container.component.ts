import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {combineLatest, Observable, of, Subscription} from 'rxjs';
import {first, tap} from 'rxjs/operators';
import {DoublesPairService} from '../service/doubles-pair.service';
import {DoublesPairInfoService} from '../service/doubles-pair-info.service';
import {TournamentEventConfigService} from '../../tournament-config/tournament-event-config.service';
import {TournamentEvent} from '../../tournament-config/tournament-event.model';
import {TournamentEventEntry} from '../model/tournament-event-entry.model';
import {TournamentEventEntryService} from '../service/tournament-event-entry.service';
import {LinearProgressBarService} from '../../../shared/linear-progress-bar/linear-progress-bar.service';
import {TournamentEntryInfoService} from '../../service/tournament-entry-info.service';
import {TournamentEntryInfo} from '../../model/tournament-entry-info.model';
import {DoublesPairInfo} from '../model/doubles-pair-info.model';
import {DoublesPair} from '../model/doubles-pair.model';

@Component({
  selector: 'app-doubles-teams-container',
  template: `
    <app-doubles-teams [doublesEvents]="doublesEvents$ | async"
                       [doublesEventEntries]="doublesEventEntries$ | async"
                       [doublesPairInfos]="doublesPairInfos$ | async"
                       [tournamentEntryInfos]="tournamentEntryInfos$ | async"
                       [selectedDoublesEventId]="selectedDoublesEventId"
                       (selectionChangeEmitter)="onEventSelectionChange($event)"
                       (makePairEmitter)="onMakePair($event)"
                       (breakPairEmitter)="onBreakPair($event)"
    >
    </app-doubles-teams>
  `,
  styles: []
})
export class DoublesTeamsContainerComponent implements OnInit, OnDestroy {

  // doubles event information
  doublesEvents$: Observable<TournamentEvent[]>;

  // entries into doubles events for currently selected doubles event
  doublesEventEntries$: Observable<TournamentEventEntry[]>;

  // entry infos with first and last name of player for one event
  tournamentEntryInfos$: Observable<TournamentEntryInfo[]>;

  // doubles pairs for this event
  doublesPairInfos$: Observable<DoublesPairInfo[]>;

  // currently selected event
  selectedDoublesEventId: number;

  private tournamentId: number;

  private loading$: Observable<boolean>;

  private subscriptions: Subscription = new Subscription();

  constructor(private doublesPairService: DoublesPairService,
              private doublesPairInfoService: DoublesPairInfoService,
              private tournamentEventConfigService: TournamentEventConfigService,
              private tournamentEventEntryService: TournamentEventEntryService,
              private tournamentEntryInfoService: TournamentEntryInfoService,
              private activatedRoute: ActivatedRoute,
              private linearProgressBarService: LinearProgressBarService) {
    const strTournamentId = this.activatedRoute.snapshot.params['tournamentId'] || 0;
    this.tournamentId = Number(strTournamentId);
    this.setupProgressIndicator();
    this.loadTournamentDoublesEvents();
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  /**
   *
   * @private
   */
  private setupProgressIndicator() {
    // if any of the service are loading show the loading progress
    this.loading$ = combineLatest(
      this.tournamentEventConfigService.store.select(this.tournamentEventConfigService.selectors.selectLoading),
      this.tournamentEventEntryService.store.select(this.tournamentEventEntryService.selectors.selectLoading),
      this.doublesPairInfoService.store.select(this.doublesPairInfoService.selectors.selectLoading),
      (eventConfigsLoading: boolean, eventEntriesLoading: boolean, doublesPairInfosLoading: boolean) => {
        return eventConfigsLoading || eventEntriesLoading || doublesPairInfosLoading;
      }
    );

    const loadingSubscription = this.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
  }

  /**
   *
   * @private
   */
  private loadTournamentDoublesEvents() {
    const subscription = this.tournamentEventConfigService.loadTournamentDoublesEvents(this.tournamentId)
      .subscribe(
        (events: TournamentEvent[]) => {
          this.doublesEvents$ = of(events);
          // load the first doubles event
          if (events.length > 0) {
            const firstDoublesEvent = events[0];
            this.loadFirstEventData(firstDoublesEvent.id);
          }
        },
        error => {
          console.log('error loading doubles events for tournament ' + this.tournamentId + ' error ' + error);
        });
    this.subscriptions.add(subscription);
  }

  loadFirstEventData(eventId: number) {
    this.doublesPairInfos$ = this.doublesPairInfoService.store.select(this.doublesPairInfoService.selectors.selectEntities);
    this.doublesEventEntries$ = this.tournamentEventEntryService.store.select(this.tournamentEventEntryService.selectors.selectEntities);
    this.onEventSelectionChange(eventId);
  }

  /**
   * Loads data for one event
   * @param eventId
   */
  onEventSelectionChange(eventId: number) {
    this.selectedDoublesEventId = eventId;
    this.loadDoublesPairInfos(eventId);
    this.loadDoublesEventEntries(eventId);
    this.loadEntryInfos(eventId);
  }

  private loadDoublesPairInfos(eventId: number) {
    this.doublesPairInfoService.loadForEvent(eventId)
      .pipe(tap((infos: DoublesPairInfo[]) => {
        console.log('got ' + infos.length + ' doubles pairs infos');
      }));
  }

  /**
   * Loads entries for a single doubles event
   * @param eventId
   * @private
   */
  private loadDoublesEventEntries(eventId: number) {
    this.tournamentEventEntryService.loadEntriesForEvent(eventId)
      .subscribe((entries: TournamentEventEntry[]) => {
      }, error => {
        console.log('Error loading tournament entries ' + error);
      });
  }

  /**
   * Loads entry infos with names and individual ratings
   * @param eventId
   * @private
   */
  private loadEntryInfos(eventId: number) {
    this.tournamentEntryInfos$ = this.tournamentEntryInfoService.getAllForEvent(eventId);
  }

  /**
   * Makes a new doubles pair
   * @param doublesPair
   * @private
   */
  public onMakePair(doublesPair: DoublesPair) {
    const subscription = this.doublesPairService.add(doublesPair)
      .pipe(first())
      .subscribe(() => {
        this.onEventSelectionChange(this.selectedDoublesEventId);
      });
    this.subscriptions.add(subscription);
  }

  /**
   * Deletes specified doubles pair
   * @param doublesPair
   * @private
   */
  public onBreakPair(doublesPair: DoublesPair) {
    const subscription = this.doublesPairService.delete(doublesPair)
      .pipe(first())
      .subscribe(() => {
        this.onEventSelectionChange(this.selectedDoublesEventId);
      });
    this.subscriptions.add(subscription);
  }
}
