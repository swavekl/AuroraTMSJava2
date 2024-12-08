import {Component, OnDestroy, OnInit} from '@angular/core';
import {Observable, Subscription} from 'rxjs';

import {TournamentConfigService} from '../../tournament/tournament-config/tournament-config.service';
import {Tournament} from '../../tournament/tournament-config/tournament.model';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';

@Component({
  selector: 'app-referee-tournament-list-container',
  template: `
    <app-referee-tournament-list [tournaments]="tournaments$ | async">
      referee-tournament-list-container works!
    </app-referee-tournament-list>
  `,
  styles: ``
})
export class RefereeTournamentListContainerComponent implements OnInit, OnDestroy {
  tournaments$: Observable<Tournament[]>;
  private loading$: Observable<boolean>;

  private subscriptions: Subscription = new Subscription();

  constructor(private tournamentConfigService: TournamentConfigService,
              private linearProgressBarService: LinearProgressBarService) {
    this.setupProgressIndicator();
  }

  ngOnInit(): void {
    this.tournaments$ = this.tournamentConfigService.getAll();
  }

  ngOnDestroy() {
    this.subscriptions.unsubscribe();
  }

  /**
   *
   * @private
   */
  private setupProgressIndicator() {
    this.loading$ = this.tournamentConfigService.store.select(this.tournamentConfigService.selectors.selectLoading);

    const loadingSubscription = this.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
  }
}
