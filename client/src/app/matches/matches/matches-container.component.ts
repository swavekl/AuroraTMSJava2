import {Component, OnDestroy, OnInit} from '@angular/core';
import {combineLatest, Observable, Subscription} from 'rxjs';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {TournamentEventConfigService} from '../../tournament/tournament-config/tournament-event-config.service';
import {ActivatedRoute} from '@angular/router';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {MatchCardService} from '../service/match-card.service';
import {MatchCard} from '../model/match-card.model';

@Component({
  selector: 'app-matches-container',
  template: `
    <app-matches [tournamentName]="tournamentName"
                 [tournamentEvents]="tournamentEvents$ | async"
                 [matchCards]="matchCards$ | async"
                 [selectedMatchCard]="selectedMatchCard$ | async"
                 (tournamentEventEmitter)="onTournamentEventSelected($event)"
                 (matchCardEmitter)="onMatchCardSelected($event)">
    </app-matches>
  `,
  styles: []
})
export class MatchesContainerComponent implements OnInit, OnDestroy {

  // name of the tournament
  tournamentName: string;

  // list of tournament events
  tournamentEvents$: Observable<TournamentEvent[]>;

  // match cards for selected event - without matches
  matchCards$: Observable<MatchCard[]>;

  // selected match card - with matches
  selectedMatchCard$: Observable<MatchCard>;

  private tournamentId: number;

  private loading$: Observable<boolean>;

  private subscriptions: Subscription = new Subscription();

  constructor(private tournamentEventConfigService: TournamentEventConfigService,
              private activatedRoute: ActivatedRoute,
              private matchCardService: MatchCardService,
              private linearProgressBarService: LinearProgressBarService) {
    this.tournamentName = history?.state?.tournamentName;
    this.setupProgressIndicator();
    this.loadTournamentEvents();
    this.setupMatchCards();
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
      // remove all events from cache in case they entered any scores and go to edit draws
      this.tournamentEventConfigService.clearCache();
  }
  /**
   *
   * @private
   */
  private setupProgressIndicator() {
    // if any of the service are loading show the loading progress
    this.loading$ = combineLatest(
      this.tournamentEventConfigService.store.select(this.tournamentEventConfigService.selectors.selectLoading),
      this.matchCardService.store.select(this.matchCardService.selectors.selectLoading),
      (eventConfigsLoading: boolean, drawsLoading: boolean) => {
        return eventConfigsLoading || drawsLoading;
      }
    );

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

  private setupMatchCards() {
    this.matchCardService.clearCache();
    this.matchCards$ = this.matchCardService.store.select(this.matchCardService.selectors.selectEntities);
  }

  onTournamentEventSelected(eventId: number) {
    this.matchCardService.loadForEvent(eventId);
  }

  onMatchCardSelected(matchCardId: number) {
    this.selectedMatchCard$ = this.matchCardService.getByKey(matchCardId);
  }
}
