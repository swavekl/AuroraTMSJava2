import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
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
    <app-player-matches
      [matchCard]="matchCard$ | async"
      [doubles]="doubles"
      [expandedMatchIndex]="expandedMatchIndex"
      (back)="onGoBack()">
    </app-player-matches>
  `,
  styles: []
})
export class PlayerMatchesContainerComponent implements OnInit, OnDestroy {

  public matchCard$: Observable<MatchCard>;

  private matchCardId: number;

  public expandedMatchIndex: number;

  private subscriptions: Subscription = new Subscription();

  public doubles: boolean;

  constructor(private activatedRoute: ActivatedRoute,
              private linearProgressBarService: LinearProgressBarService,
              private matchCardService: MatchCardService,
              private router: Router) {
    this.matchCardId = this.activatedRoute.snapshot.params['matchCardId'] || 0;
    this.doubles = (history?.state?.doubles === true);
    this.expandedMatchIndex = isNaN(history?.state?.matchIndex) ? 0 : parseInt(history.state.matchIndex, 10);
    // console.log('numeric expandedMatchIndex', {num: this.expandedMatchIndex});
    this.setupProgressIndicator();
    this.loadMatchesInformation(this.matchCardId);
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

  private loadMatchesInformation(matchCardId: number) {
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

  onGoBack() {
    const url = `today/playerschedule/detail/${this.matchCardId}`;
    const extras = {
      state: {
        doubles: this.doubles
      }
    };
    this.router.navigateByUrl(url, extras);
  }
}
