import {Component, OnDestroy, OnInit} from '@angular/core';
import {TournamentEvent} from '../../../tournament/tournament-config/tournament-event.model';
import {combineLatest, Observable, of, Subscription} from 'rxjs';
import {DrawItem} from '../../draws-common/model/draw-item.model';
import {ActivatedRoute} from '@angular/router';
import {DrawService} from '../../draws-common/service/draw.service';
import {TournamentEventConfigService} from '../../../tournament/tournament-config/tournament-event-config.service';
import {LinearProgressBarService} from '../../../shared/linear-progress-bar/linear-progress-bar.service';
import {DrawType} from '../../draws-common/model/draw-type.enum';
import {createSelector} from '@ngrx/store';
import {Tournament} from '../../../tournament/tournament-config/tournament.model';
import {first} from 'rxjs/operators';

@Component({
    selector: 'app-draws-view-detail-container',
    template: `
    <app-draws-view-detail [selectedEvent]="tournamentEvent$ | async"
                           [draws]="draws$ | async">
    </app-draws-view-detail>
  `,
    styles: [],
    standalone: false
})
export class DrawsViewDetailContainerComponent implements OnInit, OnDestroy {

  public tournamentEvent$: Observable<TournamentEvent>;

  public draws$: Observable<DrawItem[]>;

  private loading$: Observable<boolean>;

  private subscriptions: Subscription = new Subscription();

  constructor(private activatedRoute: ActivatedRoute,
              private drawService: DrawService,
              private tournamentEventConfigService: TournamentEventConfigService,
              private linearProgressBarService: LinearProgressBarService) {
    const strTournamentId = this.activatedRoute.snapshot.params['tournamentId'] || 0;
    const strEventId = this.activatedRoute.snapshot.params['eventId'] || 0;
    const tournamentId = Number(strTournamentId);
    const eventId = Number(strEventId);
    this.setupProgressIndicator();
    this.loadEventInformation(tournamentId, eventId);
  }

  /**
   *
   * @private
   */
  private setupProgressIndicator() {
    // if any of the service are loading show the loading progress
    this.loading$ = combineLatest([
        this.tournamentEventConfigService.store.select(this.tournamentEventConfigService.selectors.selectLoading),
        this.drawService.store.select(this.drawService.selectors.selectLoading)
      ],
      (eventConfigsLoading: boolean, drawsLoading: boolean) => {
        return eventConfigsLoading || drawsLoading;
      }
    );

    const loadingSubscription = this.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  /**
   *
   * @param tournamentId
   * @param eventId
   * @private
   */
  private loadEventInformation(tournamentId: number, eventId: number) {
    this.tournamentEventConfigService.store.select(this.tournamentEventConfigService.selectors.selectLoading);
    const selectedEntrySelector = createSelector(
      this.tournamentEventConfigService.selectors.selectEntityMap,
      (tournamentEvents) => {
        return tournamentEvents[eventId];
      });
    this.tournamentEvent$ = this.tournamentEventConfigService.store.select(selectedEntrySelector);
    const subscription = this.tournamentEvent$.subscribe((tournamentEvent: TournamentEvent) => {
      if (tournamentEvent == null) {
        console.log('loading all events from server for tournament ' + tournamentId);
        // get from the server if not cached yet
        this.tournamentEventConfigService.loadTournamentEvents(tournamentId).pipe(first()).subscribe();
      } else {
        // now load draws of correct type
        const drawType: DrawType = tournamentEvent.singleElimination ? DrawType.SINGLE_ELIMINATION : DrawType.ROUND_ROBIN;
        this.draws$ = this.drawService.loadForEvent(eventId, drawType);
      }
    });
    this.subscriptions.add(subscription);
  }
}
