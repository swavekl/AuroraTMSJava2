import {Component, OnDestroy, OnInit} from '@angular/core';
import {Observable, of, Subscription} from 'rxjs';
import {MatchCard} from '../model/match-card.model';
import {ActivatedRoute, Router} from '@angular/router';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {MatchCardService} from '../service/match-card.service';
import {createSelector} from '@ngrx/store';
import {Match} from '../model/match.model';
import {MatchService} from '../service/match.service';

@Component({
  selector: 'app-score-entry-phone-container',
  template: `
    <app-score-entry-phone
      [match]="match$ | async"
      [playerAName]="playerAName$ | async"
      [playerBName]="playerBName$ | async"
      [numberOfGames]="numberOfGames"
      [pointsPerGame]="pointsPerGame"
      [doubles]="doubles"
    (saveMatch)="onSaveMatch($event)"
    (cancelMatch)="onCancelMatch()">
    </app-score-entry-phone>
  `,
  styles: []
})
export class ScoreEntryPhoneContainerComponent implements OnInit, OnDestroy {

  public match$: Observable<Match>;

  public playerAName$: Observable<String>;
  public playerBName$: Observable<String>;

  private tournamentId: number;
  private matchCardId: number;
  private matchIndex: number;
  public numberOfGames: number;
  public pointsPerGame: number;

  public doubles: boolean;
  private subscriptions: Subscription = new Subscription();

  constructor(private activatedRoute: ActivatedRoute,
              private linearProgressBarService: LinearProgressBarService,
              private matchCardService: MatchCardService,
              private matchService: MatchService,
              private router: Router) {
    this.tournamentId = this.activatedRoute.snapshot.params['tournamentId'] || 0;
    this.matchCardId = this.activatedRoute.snapshot.params['matchCardId'] || 0;
    this.matchIndex = this.activatedRoute.snapshot.params['matchIndex'] || 0;
    this.doubles = (history?.state?.doubles === true);
    this.pointsPerGame = 11;
    this.numberOfGames = 5;
    this.setupProgressIndicator();
    this.loadMatchInformation(this.matchCardId, this.matchIndex);
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

  /**
   *
   * @param matchCardId
   * @param matchIndex
   * @private
   */
  private loadMatchInformation(matchCardId: number, matchIndex: number) {
    this.matchCardService.store.select(this.matchCardService.selectors.selectLoading);
    const selectedEntrySelector = createSelector(
      this.matchCardService.selectors.selectEntityMap,
      (entityMap) => {
        return entityMap[matchCardId];
      });
    const matchCard$: Observable<MatchCard> = this.matchCardService.store.select(selectedEntrySelector);
    const subscription = matchCard$.subscribe((matchCard: MatchCard) => {
      if (matchCard == null) {
        // console.log('getting match card from server');
        // get from the server if not cached yet
        this.matchCardService.getByKey(matchCardId);
      } else {
        console.log('get match card from cache');
        const allMatches = matchCard.matches;
        if (this.matchIndex < allMatches.length) {
          const match = allMatches[matchIndex];
          console.log('cloning match');
          const cloneOfMatch = JSON.parse(JSON.stringify(match));
          this.match$ = of(cloneOfMatch);
          // console.log('match is', match);
          this.playerAName$ = of(matchCard.profileIdToNameMap[match.playerAProfileId]);
          this.playerBName$ = of(matchCard.profileIdToNameMap[match.playerBProfileId]);
        }
        this.numberOfGames = matchCard.numberOfGames;
      }
    });
    this.subscriptions.add(subscription);
  }

  onSaveMatch(updatedMatch: Match) {
    const subscription: Subscription = this.matchService.update(updatedMatch)
      .subscribe((match: Match) => {
        // reload the match card with this match
        this.matchCardService.getByKey(this.matchCardId);
        this.backToMatchCard();
      });
    this.subscriptions.add(subscription);
  }

  onCancelMatch() {
    this.backToMatchCard();
  }

  private backToMatchCard() {
    const url = `/ui/matches/playermatches/${this.tournamentId}/${this.matchCardId}`;
    const extras = {
      state: {
        doubles: this.doubles,
        matchIndex: this.matchIndex
      }
    };
    this.router.navigateByUrl(url, extras);
  }
}
