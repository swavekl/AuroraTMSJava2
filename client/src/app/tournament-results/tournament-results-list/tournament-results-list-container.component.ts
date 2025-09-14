import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {Observable, of, Subscription} from 'rxjs';
import {first} from 'rxjs/operators';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {TournamentResultsService} from '../service/tournament-results.service';
import {EventResultStatus} from '../model/event-result-status';

@Component({
    selector: 'app-tournament-results-container',
    template: `
    <app-tournament-results [eventResultStatusList]="eventResultStatusList$ | async"
    [tournamentId]="tournamentId">
    </app-tournament-results>
  `,
    styles: [],
    standalone: false
})
export class TournamentResultsListContainerComponent implements OnInit, OnDestroy {

  private subscriptions: Subscription = new Subscription();
  tournamentId: number;

  eventResultStatusList$: Observable<EventResultStatus[]>;

  constructor(private linearProgressBarService: LinearProgressBarService,
              private activatedRoute: ActivatedRoute,
              private tournamentResultsService: TournamentResultsService) {
    const strTournamentId = this.activatedRoute.snapshot.params['tournamentId'] || 0;
    this.tournamentId = Number(strTournamentId);
    this.setupProgressIndicator();
    this.loadTournamentResults();
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  private setupProgressIndicator() {
    const loadingSubscription = this.tournamentResultsService.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
  }

  private loadTournamentResults() {
    const subscription = this.tournamentResultsService.getTournamentResults(this.tournamentId)
      .pipe(first())
      .subscribe(
        (results: EventResultStatus[]) => {
          this.eventResultStatusList$ = of(results);
      }, (error: any) => {
          console.log('error generating draws ' + error);
        });
    this.subscriptions.add(subscription);
  }
}
