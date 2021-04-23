import {Component, OnDestroy, OnInit} from '@angular/core';
import {TournamentEventConfigService} from '../../tournament/tournament-config/tournament-event-config.service';
import {combineLatest, Observable, Subscription} from 'rxjs';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {ActivatedRoute} from '@angular/router';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';

@Component({
  selector: 'app-draws-container',
  template: `
    <p>
      <app-draws [tournamentEvents]="tournamentEvents$ | async"></app-draws>
    </p>
  `,
  styles: [
  ]
})
export class DrawsContainerComponent implements OnInit, OnDestroy {

  tournamentEvents$: Observable<TournamentEvent[]>;

  private tournamentId: number;

  private loading$: Observable<boolean>;

  private subscriptions: Subscription = new Subscription();


  constructor(private tournamentEventConfigService: TournamentEventConfigService,
              private activatedRoute: ActivatedRoute,
              private linearProgressBarService: LinearProgressBarService) {
    this.setupProgressIndicator();
    this.loadTournamentEvents();
  }

  /**
   *
   * @private
   */
  private setupProgressIndicator() {
    // if any of the service are loading show the loading progress
    this.loading$ = combineLatest(
      this.tournamentEventConfigService.store.select(this.tournamentEventConfigService.selectors.selectLoading),
      (eventConfigsLoading: boolean) => {
        return eventConfigsLoading;
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

  private loadTournamentEvents() {
    const strTournamentId = this.activatedRoute.snapshot.params['tournamentId'] || 0;
    this.tournamentId = Number(strTournamentId);
    this.tournamentEvents$ = this.tournamentEventConfigService.store.select(
      this.tournamentEventConfigService.selectors.selectEntities);
    // load them - they will surface via this selector
    this.tournamentEventConfigService.loadTournamentEvents(this.tournamentId);
  }
}
