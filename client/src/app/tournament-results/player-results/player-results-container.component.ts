import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {combineLatest, Observable, of, Subscription} from 'rxjs';
import {first} from 'rxjs/operators';
import {createSelector} from '@ngrx/store';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {TournamentResultsService} from '../service/tournament-results.service';
import {PlayerMatchSummary} from '../model/player-match-summary';
import {TournamentEntryService} from '../../tournament/tournament-entry/service/tournament-entry.service';
import {TournamentEntry} from '../../tournament/tournament-entry/model/tournament-entry.model';

@Component({
  selector: 'app-player-results-container',
  template: `
    <app-player-results
      [playerMatchSummaryList]="playerMatchSummaries$ | async"
      [tournamentEntry]="entry$ | async">
    </app-player-results>
  `,
  styles: []
})
export class PlayerResultsContainerComponent implements OnInit, OnDestroy {

  private entryId: number;
  private profileId: string;

  playerMatchSummaries$: Observable<PlayerMatchSummary[]>;

  entry$: Observable<TournamentEntry>;

  private loading$: Observable<boolean>;
  private subscriptions: Subscription = new Subscription();

  constructor(private linearProgressBarService: LinearProgressBarService,
              private activatedRoute: ActivatedRoute,
              private tournamentResultsService: TournamentResultsService,
              private tournamentEntryService: TournamentEntryService) {
    const strEntryId = this.activatedRoute.snapshot.params['entryId'] || 0;
    this.entryId = Number(strEntryId);
    this.profileId = this.activatedRoute.snapshot.params['profileId'];
    this.setupProgressIndicator();
    this.loadPlayerResults();
    this.loadTournamentEntry(this.entryId);
  }

  private setupProgressIndicator() {
    // if any of the service are loading show the loading progress
    this.loading$ = combineLatest(
      this.tournamentResultsService.loading$,
      this.tournamentEntryService.store.select(this.tournamentEntryService.selectors.selectLoading),
      (resultsLoading: boolean, eventEntryLoading: boolean) => {
        return resultsLoading || eventEntryLoading;
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

  private loadPlayerResults() {
    this.playerMatchSummaries$ = this.tournamentResultsService.getPlayerTournamentResults(this.entryId, this.profileId);
  }

  private loadTournamentEntry(entryId: number) {
    // see if entry is cached on the client already
    // construct a selector to pick this one entry from cache
    const entityMapSelector = this.tournamentEntryService.selectors.selectEntityMap;
    const selectedEntrySelector = createSelector(
      entityMapSelector,
      (entityMap) => {
        return entityMap[entryId];
      });
    const localEntry$: Observable<TournamentEntry> = this.tournamentEntryService.store.select(selectedEntrySelector);
    const subscription = localEntry$
      .pipe(first())
      .subscribe((tournamentEntry: TournamentEntry) => {
      // editing - check if we had it in cache if not - then fetch it
      if (!tournamentEntry) {
        this.entry$ = this.tournamentEntryService.getByKey(entryId);
      } else {
        this.entry$ = of (tournamentEntry);
      }
    });
    this.subscriptions.add(subscription);
  }
}
