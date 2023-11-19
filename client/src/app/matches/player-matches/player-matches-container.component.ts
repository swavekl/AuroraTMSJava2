import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {MatchCardService} from '../service/match-card.service';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {combineLatest, Observable, of, Subscription} from 'rxjs';
import {createSelector} from '@ngrx/store';
import {MatchCard} from '../model/match-card.model';
import {TournamentEventConfigService} from '../../tournament/tournament-config/tournament-event-config.service';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {first, map, switchMap, tap} from 'rxjs/operators';
import {MatchService} from '../service/match.service';
import {Match} from '../model/match.model';
import {ConfirmationPopupComponent} from '../../shared/confirmation-popup/confirmation-popup.component';
import {MatDialog} from '@angular/material/dialog';
import {AuthenticationService} from '../../user/authentication.service';

/**
 * List of matches for a single match card for viewing by player
 */
@Component({
  selector: 'app-player-matches-container',
  template: `
    <app-player-matches
      [matchCard]="matchCard$ | async"
      [tournamentId]="tournamentId"
      [doubles]="doubles"
      [tournamentEvent]="tournamentEvent$ | async"
      [pointsPerGame]="pointsPerGame"
      [expandedMatchIndex]="expandedMatchIndex"
      (updateMatch)="onUpdateMatch($event)"
      (back)="onGoBack()"
      (enterMatchScore)="onEnterScore($event)"
      (rankings)="navigateToRankings()"
      (refresh)="onRefresh()">
    </app-player-matches>
  `,
  styles: []
})
export class PlayerMatchesContainerComponent implements OnInit, OnDestroy {

  public matchCard$: Observable<MatchCard>;
  private matchCard: MatchCard;

  private loading$: Observable<boolean>;

  private matchCardId: number;

  public expandedMatchIndex: number;

  public doubles: boolean;

  public pointsPerGame: number;

  public tournamentId: number;
  public tournamentEntryId: number;
  public tournamentDay: number;

  private returnUrl: string;

  tournamentEvent$: Observable<TournamentEvent>;
  private tournamentEvent: TournamentEvent;

  private subscriptions: Subscription = new Subscription();

  // indicates if we are coming to this screen from score entry dialog
  private fromMatchSave: boolean;

  private thisPlayerProfileId: string;

  constructor(private activatedRoute: ActivatedRoute,
              private linearProgressBarService: LinearProgressBarService,
              private matchCardService: MatchCardService,
              private matchService: MatchService,
              private tournamentEventConfigService: TournamentEventConfigService,
              private authenticationService: AuthenticationService,
              private dialog: MatDialog,
              private router: Router) {
    this.tournamentId = this.activatedRoute.snapshot.params['tournamentId'] || 0;
    this.tournamentDay = this.activatedRoute.snapshot.params['tournamentDay'] || 1;
    this.tournamentEntryId = this.activatedRoute.snapshot.params['tournamentEntryId'] || 0;
    this.matchCardId = this.activatedRoute.snapshot.params['matchCardId'] || 0;
    this.doubles = (history?.state?.doubles === true);
    this.fromMatchSave = !isNaN(history?.state?.matchIndex);
    this.expandedMatchIndex = isNaN(history?.state?.matchIndex) ? 0 : parseInt(history.state.matchIndex, 10);
    this.returnUrl = `/ui/today/playerscheduledetail/${this.tournamentId}/${this.tournamentDay}/${this.tournamentEntryId}/${this.matchCardId}`;
    this.pointsPerGame = 11;
    this.thisPlayerProfileId = this.authenticationService.getCurrentUserProfileId();
    this.setupProgressIndicator();
    this.loadMatchesInformation(this.matchCardId, true);
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  private setupProgressIndicator() {
    this.loading$ = combineLatest(
      this.tournamentEventConfigService.store.select(this.tournamentEventConfigService.selectors.selectLoading),
      this.matchCardService.store.select(this.matchCardService.selectors.selectLoading),
      (eventConfigLoading: boolean, matchCardLoading: boolean) => {
        return eventConfigLoading || matchCardLoading;
      }
    );

    const loadingSubscription = this.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
  }

  /**
   *
   * @param matchCardId
   * @param loadEventInformation
   * @private
   */
  private loadMatchesInformation(matchCardId: number, loadEventInformation: boolean) {
    // always load the match card information so it is up to date
    // this is to detect if a player started entering scores to prevent the other player from overwriting it
    this.matchCard$ = this.matchCardService.getByKey(matchCardId)
      .pipe(
        tap((matchCard: MatchCard) => {
          if (matchCard != null) {
            // console.log('MAIN got match card', matchCard);
            this.matchCard = matchCard;
            // console.log('loading event information for event ', matchCard.eventFk);
            if (loadEventInformation) {
              this.loadEventInformation(this.tournamentId, matchCard);
            }
          }
        return matchCard;
    }));
  }

  /**
   *
   * @param tournamentId
   * @param eventId
   * @private
   */
  private loadEventInformation(tournamentId: number, matchCard: MatchCard) {
    const eventId: number =  matchCard.eventFk;
    this.tournamentEventConfigService.store.select(this.tournamentEventConfigService.selectors.selectLoading);
    const selectedEntrySelector = createSelector(
      this.tournamentEventConfigService.selectors.selectEntityMap,
      (tournamentEvents) => {
        return tournamentEvents[eventId];
      });
    this.tournamentEvent$ = this.tournamentEventConfigService.store.select(selectedEntrySelector);
    const subscription = this.tournamentEvent$.subscribe((tournamentEvent: TournamentEvent) => {
      if (tournamentEvent == null) {
        // console.log('loading events from server for tournament ' + tournamentId);
        // get from the server if not cached yet
        this.tournamentEventConfigService.loadTournamentEvents(tournamentId).pipe(first()).subscribe();
      } else {
        // console.log('got event from cache');
        this.pointsPerGame = tournamentEvent.pointsPerGame;
        this.tournamentId = tournamentEvent.tournamentFk;
        this.doubles = tournamentEvent.doubles;
        this.tournamentEvent = tournamentEvent;
        if (this.fromMatchSave) {
          this.checkIfMatchCardCompleted(matchCard);
        }
      }
    });
    this.subscriptions.add(subscription);
  }

  onGoBack() {
    const extras = {
      state: {
        doubles: this.doubles
      }
    };
    this.router.navigateByUrl(this.returnUrl, extras);
  }

  onEnterScore(event) {
    const matchId = event.matchId;
    const matchIndex = event.matchIndex;
    // console.log('checking match with id if started', matchId);
    if (this.matchCard != null) {
      const match: Match = this.matchCard.matches[matchIndex];
      const entryNotStarted = match.scoreEnteredByProfileId == null || match.scoreEnteredByProfileId == '';
      const entryByMe = (match.scoreEnteredByProfileId === this.thisPlayerProfileId);
      // console.log('entryNotStarted', entryNotStarted);
      // console.log('entryByMe', entryByMe);
      if (entryNotStarted || entryByMe) {
        if (entryByMe) {
          this.gotoScoreEntry(matchIndex);
        } else if (entryNotStarted) {
          // immediately update the match
          const subscription: Subscription = this.matchService.lockMatch(matchId, this.thisPlayerProfileId)
            .pipe(first(),
              switchMap(
                (lockedMatch: boolean) => {
                  // console.log('match is successfully locked', lockedMatch);
                  // console.log('reloading match card with id', this.matchCardId);
                  return this.matchCardService.getByKey(this.matchCardId)
                    .pipe(first(),
                      map((matchCard: MatchCard) => {
                        // console.log('reloaded match card');
                        // this.matchCard = matchCard;
                        // const matches: Match[] = matchCard.matches;
                        // const thisMatch = matches[matchIndex];
                        // console.log('thisMatch.scoreEnteredByProfileId', thisMatch.scoreEnteredByProfileId);
                        // console.log('this.thisPlayerProfileId         ', this.thisPlayerProfileId);
                        if (lockedMatch) {
                          this.gotoScoreEntry(matchIndex);
                        } else {
                          this.showWarningDialog();
                        }
                        return matchCard;
                      }));
                }))
            .subscribe();
          this.subscriptions.add(subscription);
        }
      } else {
        this.showWarningDialog();
      }
    }
  }

  private gotoScoreEntry(matchIndex: number) {
    const url = `/ui/matches/scoreentryphone/${this.tournamentId}/${this.tournamentDay}/${this.tournamentEntryId}/${this.matchCardId}/${matchIndex}`;
    const extras = {
      state: {
        doubles: this.doubles
      }
    };
    this.router.navigateByUrl(url, extras);
  }

  private showWarningDialog() {
    const message = `You can't enter/change score because, another player or tournament official already started entering score.`;
    const config = {
      width: '300px', height: '240px', data: {
        title: 'Error',
        message: message, contentAreaHeight: 80, showCancel: false,
        okText: 'Close'
      }
    };
    const dialogRef = this.dialog.open(ConfirmationPopupComponent, config);
    dialogRef.afterClosed().subscribe(result => {
      this.loadMatchesInformation(this.matchCardId, false);
    });
  }

  /**
   * Handles defaulting
   * @param updatedMatch
   */
  public onUpdateMatch(updatedMatch: Match) {
    this.matchService.update(updatedMatch)
      .pipe(
        switchMap((match: Match) => {
          return this.matchCardService.getByKey(this.matchCardId)
            .pipe(
              first(),
              map((matchCard: MatchCard) => {
                if (matchCard != null) {
                  this.matchCard = matchCard;
                  this.checkIfMatchCardCompleted(matchCard);
                }
                return matchCard;
              }) );

        })).subscribe();
  }

  private checkIfMatchCardCompleted(matchCard: MatchCard) {
    let completed = MatchCard.isMatchCardCompleted(matchCard, this.tournamentEvent);
    let completedButDefaults = MatchCard.isMatchCardCompletedExceptForDefaults(matchCard, this.tournamentEvent);
    // console.log('completed', completed);
    // console.log('completedButDefaults', completedButDefaults);
    if (completed) {
      const config = {
        width: '450px', height: '230px', data: {
          message: `We will now rank and advance players.`,
          showCancel: false, okText: 'Close', title: 'Match Card Complete'
        }
      };
      const dialogRef = this.dialog.open(ConfirmationPopupComponent, config);
      dialogRef.afterClosed().subscribe(result => {
        if (result === 'ok') {
          this.navigateToRankings();
        }
      });
    } else if (completedButDefaults) {
      const config = {
        width: '450px', height: '310px', data: {
          message: `There are matches in this group where both players are absent and these can only be entered at the control desk.
           Please inform control desk personnel to default these players so the final player rankings can be computed. Thank you.`,
          showCancel: false, okText: 'Close', title: 'Unfinished Matches', contentAreaHeight: '190px'
        }
      };
      const dialogRef = this.dialog.open(ConfirmationPopupComponent, config);
      dialogRef.afterClosed().subscribe(result => {
      });
    }
  }

  public navigateToRankings() {
    const returnUrl = `/ui/today/playerschedule/${this.tournamentId}/${this.tournamentDay}/${this.tournamentEntryId}`;
    const extras = {
      state: {
        returnUrl: returnUrl,
        eventName: this.tournamentEvent.name
      }
    };
    const url = `/ui/matches/rankingresults/${this.matchCardId}`;
    this.router.navigateByUrl(url, extras);
  }

  public onRefresh() {
    this.loadMatchesInformation(this.matchCardId, false);
  }
}
