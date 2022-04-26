import {Component, OnDestroy, OnInit} from '@angular/core';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {ActivatedRoute} from '@angular/router';
import {TournamentResultsService} from '../service/tournament-results.service';
import {combineLatest, Observable, of, Subscription} from 'rxjs';
import {first} from 'rxjs/operators';
import {MatchCardService} from '../../matches/service/match-card.service';
import {MatchCard} from '../../matches/model/match-card.model';
import {TournamentEventConfigService} from '../../tournament/tournament-config/tournament-event-config.service';
import {createSelector} from '@ngrx/store';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {EventResults} from '../model/event-results';

@Component({
  selector: 'app-tournament-result-detail-container',
  template: `
    <app-tournament-result-details
      [event]="event$ | async"
      [eventResultsList]="eventResultsList$ | async">
    </app-tournament-result-details>
  `,
  styles: []
})
export class TournamentResultDetailsContainerComponent implements OnInit, OnDestroy {

  // all event results for this event
  eventResultsList$: Observable<EventResults[]>;

  // event information
  event$: Observable<TournamentEvent>;

  private subscriptions: Subscription = new Subscription();
  private loading$: Observable<boolean>;

  constructor(private linearProgressBarService: LinearProgressBarService,
              private activatedRoute: ActivatedRoute,
              private tournamentResultsService: TournamentResultsService,
              private tournamentEventConfigService: TournamentEventConfigService) {
    const strTournamentId = this.activatedRoute.snapshot.params['tournamentId'] || 0;
    const tournamentId = Number(strTournamentId);
    const strEventId = this.activatedRoute.snapshot.params['eventId'] || 0;
    const eventId = Number(strEventId);
    this.setupProgressIndicator();
    this.loadTournamentEvent(tournamentId, eventId);
    this.loadEventResults(tournamentId, eventId);
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  private setupProgressIndicator() {
    this.loading$ = combineLatest(
      this.tournamentEventConfigService.store.select(this.tournamentEventConfigService.selectors.selectLoading),
      this.tournamentResultsService.loading$,
      (eventConfigsLoading: boolean, resultsLoading: boolean) => {
        return eventConfigsLoading || resultsLoading;
      }
    );

    const loadingSubscription = this.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
  }

  private loadTournamentEvent(tournamentId: number, eventId: number) {
    const selector = createSelector(
      this.tournamentEventConfigService.selectors.selectEntityMap,
      (entityMap) => {
        return entityMap[eventId];
      });
    const subscription = this.tournamentEventConfigService.store.select(selector)
      .subscribe((tournamentEvent: TournamentEvent) => {
        // console.log('tournamentEvent ', tournamentEvent);
        if (!tournamentEvent) {
          this.tournamentEventConfigService.getByKey(tournamentId, eventId);
        } else {
          this.event$ = of(tournamentEvent);
        }
      });
    this.subscriptions.add(subscription);
  }

  private loadEventResults(tournamentId: number, eventId: number) {
    this.eventResultsList$ = this.tournamentResultsService.getEventResults(tournamentId, eventId);
  }
}
