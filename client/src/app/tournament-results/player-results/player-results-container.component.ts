import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {Observable, Subscription} from 'rxjs';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {TournamentResultsService} from '../service/tournament-results.service';
import {PlayerMatchSummary} from '../model/player-match-summary';
import {first} from 'rxjs/operators';

@Component({
  selector: 'app-player-results-container',
  template: `
    <app-player-results [playerMatchSummaryList]="playerMatchSummaries$ | async">
    </app-player-results>
  `,
  styles: []
})
export class PlayerResultsContainerComponent implements OnInit, OnDestroy {

  private entryId: number;
  private tournamentId: number;
  private profileId: string;

  playerMatchSummaries$: Observable<PlayerMatchSummary[]>;

  private subscriptions: Subscription = new Subscription();

  constructor(private linearProgressBarService: LinearProgressBarService,
              private activatedRoute: ActivatedRoute,
              private tournamentResultsService: TournamentResultsService) {
    const strEntryId = this.activatedRoute.snapshot.params['entryId'] || 0;
    this.entryId = Number(strEntryId);
    this.profileId = this.activatedRoute.snapshot.params['profileId'];
    this.setupProgressIndicator();
    this.loadPlayerResults();
  }

  private setupProgressIndicator() {
    // subscription for indicating progress on global toolbar
    const subscription = this.tournamentResultsService.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(subscription);

  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  private loadPlayerResults() {
    this.playerMatchSummaries$ = this.tournamentResultsService.getPlayerTournamentResults(this.entryId, this.profileId);
    const subscription = this.playerMatchSummaries$.pipe(first())
      .subscribe(
        (playerMatchSummaries: PlayerMatchSummary[]) => {
          console.log('got player results', playerMatchSummaries);
        }, (error: any) => console.log ('error', error)
        );
    this.subscriptions.add(subscription);
  }
}
