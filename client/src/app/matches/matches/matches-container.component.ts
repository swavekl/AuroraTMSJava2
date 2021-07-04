import {Component, OnInit} from '@angular/core';
import {Observable, Subscription} from 'rxjs';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {TournamentEventConfigService} from '../../tournament/tournament-config/tournament-event-config.service';
import {ActivatedRoute} from '@angular/router';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {MatchCardService} from '../service/match-card.service';

@Component({
  selector: 'app-matches-container',
  template: `
    <app-matches>
    </app-matches>
  `,
  styles: [
  ]
})
export class MatchesContainerComponent implements OnInit {

  // list of tournament events
  tournamentEvents$: Observable<TournamentEvent[]>;

  // match cards for selected event
  matchCards$: Observable<any[]>;

  private tournamentId: number;

  private loading$: Observable<boolean>;

  private subscriptions: Subscription = new Subscription();

  constructor(private tournamentEventConfigService: TournamentEventConfigService,
              private activatedRoute: ActivatedRoute,
              private matchCardService: MatchCardService,
              private linearProgressBarService: LinearProgressBarService) {
    this.setupProgressIndicator();
    this.loadTournamentEvents();
    this.setupMatches();
  }

  ngOnInit(): void {
  }

  /**
   *
   * @private
   */
  private setupProgressIndicator() {
    // if any of the service are loading show the loading progress
    // this.loading$ = combineLatest(
    //   this.tournamentEventConfigService.store.select(this.tournamentEventConfigService.selectors.selectLoading),
    //   this.matchCardService.store.select(this.matchCardService.selectors.selectLoading),
    //   (eventConfigsLoading: boolean, drawsLoading: boolean) => {
    //     return eventConfigsLoading || drawsLoading;
    //   }
    // );

    const loadingSubscription = this.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
  }

  private loadTournamentEvents() {
    const strTournamentId = this.activatedRoute.snapshot.params['tournamentId'] || 0;
    this.tournamentId = Number(strTournamentId);
    this.tournamentEvents$ = this.tournamentEventConfigService.store.select(
      this.tournamentEventConfigService.selectors.selectEntities);
    // load them - they will surface via this selector
    this.tournamentEventConfigService.loadTournamentEvents(this.tournamentId);
  }

  private setupMatches () {

  }
}
