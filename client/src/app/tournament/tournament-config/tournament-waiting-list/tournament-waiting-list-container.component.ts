import {Component, OnDestroy, OnInit} from '@angular/core';
import {combineLatest, Observable, of, Subscription} from 'rxjs';
import {TournamentEntryInfo} from '../../model/tournament-entry-info.model';
import {TournamentEntryInfoService} from '../../service/tournament-entry-info.service';
import {TournamentEvent} from '../tournament-event.model';
import {first, map} from 'rxjs/operators';
import {TournamentEventConfigService} from '../tournament-event-config.service';
import {ActivatedRoute} from '@angular/router';
import {LinearProgressBarService} from '../../../shared/linear-progress-bar/linear-progress-bar.service';

@Component({
  selector: 'app-tournament-waiting-list-container',
  template: `
    <app-tournament-waiting-list
      [tournamentEntryInfos]="tournamentEntryInfos$ | async"
      [tournamentEvents]="tournamentEvents$ | async">
    </app-tournament-waiting-list>
  `,
  styles: []
})
export class TournamentWaitingListContainerComponent implements OnInit, OnDestroy {

  tournamentEntryInfos$: Observable<TournamentEntryInfo[]>;
  tournamentEvents$: Observable<TournamentEvent[]>;

  private subscriptions: Subscription = new Subscription();
  loading$: Observable<boolean>;

  constructor(private tournamentEntryInfoService: TournamentEntryInfoService,
              private tournamentEventConfigService: TournamentEventConfigService,
              private activatedRoute: ActivatedRoute,
              private linearProgressBarService: LinearProgressBarService) {
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  ngOnInit(): void {
    this.setupProgressIndicator();
    const tournamentId = this.activatedRoute.snapshot.params['tournamentId'] || 0;
    this.loadWaitingListEntries(tournamentId);
    this.loadTournamentEvents(tournamentId);
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
      .pipe(first())
      .subscribe(
        (events: TournamentEvent[]) => {
          if (events != null && events.length > 0 && events[0].tournamentFk === tournamentId) {
            this.tournamentEvents$ = of(events);
          } else {
            // don't have event configs cached - load them - again template is subscribed to this
            this.tournamentEvents$ = this.tournamentEventConfigService.loadTournamentEvents(tournamentId);
          }
        },
        (error: any) => {
          console.log('error loading tournament events ' + JSON.stringify(error));
        }
      );

    this.subscriptions.add(subscription);
  }

  /**
   * Loads all entries for players who wait to enter into any event
   * @param tournamentId
   * @private
   */
  private loadWaitingListEntries(tournamentId: number) {
    const subscription = this.tournamentEntryInfoService.getWaitingListEntries(tournamentId)
      .pipe(
        first(),
        map((infos: TournamentEntryInfo[]) => {
          this.tournamentEntryInfos$ = of(infos);
          return infos;
        })
      )
      .subscribe((infos: TournamentEntryInfo[]) => {
          // this.waitingListEntries$ = of(infos);
        },
        (error: any) => {
          console.log('error loading entry infos' + JSON.stringify(error));
        });
    this.subscriptions.add(subscription);
  }
}
