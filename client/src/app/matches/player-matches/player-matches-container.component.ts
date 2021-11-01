import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {MatchCardService} from '../service/match-card.service';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {Observable, Subscription} from 'rxjs';
import {createSelector} from '@ngrx/store';
import {MatchCard} from '../model/match-card.model';

/**
 * List of matches for a single match card for viewing by player
 */
@Component({
  selector: 'app-player-matches-container',
  template: `
      <app-player-matches [matchCard]="matchCard$ | async"></app-player-matches>
  `,
  styles: [
  ]
})
export class PlayerMatchesContainerComponent implements OnInit, OnDestroy {

  public matchCard$: Observable<MatchCard>;

  private subscriptions: Subscription = new Subscription();

  constructor(private activatedRoute: ActivatedRoute,
              private linearProgressBarService: LinearProgressBarService,
              private matchCardService: MatchCardService) {
    const matchCardId = this.activatedRoute.snapshot.params['matchCardId'] || 0;
    this.setupProgressIndicator();
    this.loadMatchesInformation(matchCardId);
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  private setupProgressIndicator() {
    const subscription = this.matchCardService.store.select(this.matchCardService.selectors.selectLoading)
      .subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(subscription);
  }

  private loadMatchesInformation (matchCardId: number) {
    this.matchCardService.store.select(this.matchCardService.selectors.selectLoading);
    const selectedEntrySelector = createSelector(
      this.matchCardService.selectors.selectEntityMap,
      (entityMap) => {
        return entityMap[matchCardId];
      });
    this.matchCard$ = this.matchCardService.store.select(selectedEntrySelector);
    const subscription = this.matchCard$.subscribe((matchCard: MatchCard) => {
      if (matchCard == null) {
        // get from the server if not cached yet
        this.matchCardService.getByKey(matchCardId);
      }
    });
    this.subscriptions.add(subscription);
    }
}
