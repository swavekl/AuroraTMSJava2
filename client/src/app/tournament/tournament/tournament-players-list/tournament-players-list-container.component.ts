import {Component, OnDestroy, OnInit} from '@angular/core';
import {TournamentEntryInfoService} from '../../service/tournament-entry-info.service';
import {ActivatedRoute} from '@angular/router';
import {combineLatest, Observable, of, Subscription} from 'rxjs';
import {LinearProgressBarService} from '../../../shared/linear-progress-bar/linear-progress-bar.service';
import {first, map} from 'rxjs/operators';
import {TournamentEntryInfo} from '../../model/tournament-entry-info.model';
import {TournamentEvent} from '../../tournament-config/tournament-event.model';
import {TournamentEventConfigService} from '../../tournament-config/tournament-event-config.service';
import * as moment from 'moment';

@Component({
  selector: 'app-tournament-player-list-container',
  template: `
    <app-tournament-players-list [entryInfos]="entryInfos$ | async"
                                 [tournamentEvents]="tournamentEvents$ | async"
                                 [tournamentStartDate]="tournamentStartDate"
    ></app-tournament-players-list>
  `,
  styles: []
})
export class TournamentPlayersListContainerComponent implements OnInit, OnDestroy {

  private subscriptions: Subscription = new Subscription();

  entryInfos$: Observable<TournamentEntryInfo[]>;
  tournamentEvents$: Observable<TournamentEvent[]>;
  tournamentStartDate: Date;

  loading$: Observable<boolean>;

  constructor(private tournamentEntryInfoService: TournamentEntryInfoService,
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
    console.log('loading tournament events');
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

  private loadTournamentStartDate(tournamentId: number) {
    this.tournamentStartDate = moment('2022-01-15').toDate();
  }
}
