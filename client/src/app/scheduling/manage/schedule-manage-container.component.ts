import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {combineLatest, Observable, of, Subscription} from 'rxjs';
import {first} from 'rxjs/operators';
import {createSelector} from '@ngrx/store';
import {Tournament} from '../../tournament/tournament-config/tournament.model';
import {TournamentConfigService} from '../../tournament/tournament-config/tournament-config.service';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {TournamentEventConfigService} from '../../tournament/tournament-config/tournament-event-config.service';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {MatchCard} from '../../matches/model/match-card.model';
import {MatchCardService} from '../../matches/service/match-card.service';
import {MatchSchedulingService} from '../service/match-scheduling.service';
import {ErrorMessagePopupService} from '../../shared/error-message-dialog/error-message-popup.service';

@Component({
  selector: 'app-schedule-manage-container',
  template: `
    <app-schedule-manage
      [tournament]="tournament$ | async"
      [tournamentEvents]="tournamentEvents$ | async"
      [matchCards]="matchCards$ | async"
      (dayChangedEvent)="onDayChangedEvent($event)"
      (generateScheduleForEvent)="onGenerateForDay($event)"
      (fixUnscheduledEvents)="onFixUnscheduledEvents($event)"
      (updateMatchCardsEvent)="onUpdateMatchCards($event)"
    >
    </app-schedule-manage>
  `,
  styles: []
})
export class ScheduleManageContainerComponent implements OnInit, OnDestroy {

  // tournament information
  tournament$: Observable<Tournament>;

  // list of tournament events
  tournamentEvents$: Observable<TournamentEvent[]>;

  // match cards for selected event
  matchCards$: Observable<MatchCard[]>;

  private subscriptions: Subscription = new Subscription();

  private loading$: Observable<boolean>;

  private tournamentId: number;

  constructor(private activatedRoute: ActivatedRoute,
              private tournamentConfigService: TournamentConfigService,
              private tournamentEventConfigService: TournamentEventConfigService,
              private matchCardService: MatchCardService,
              private matchSchedulingService: MatchSchedulingService,
              private linearProgressBarService: LinearProgressBarService,
              private errorMessagePopupService: ErrorMessagePopupService) {
    const strTournamentId = this.activatedRoute.snapshot.params['tournamentId'] || 0;
    this.tournamentId = Number(strTournamentId);
    this.setupProgressIndicator();
    this.loadTournament(this.tournamentId);
    this.loadTournamentEvents(this.tournamentId);
    this.setupMatchCards(this.tournamentId);
  }

  private setupProgressIndicator() {
    // if any of the service are loading show the loading progress
    this.loading$ = combineLatest(
      this.tournamentConfigService.store.select(this.tournamentConfigService.selectors.selectLoading),
      this.tournamentEventConfigService.store.select(this.tournamentEventConfigService.selectors.selectLoading),
      this.matchCardService.store.select(this.matchCardService.selectors.selectLoading),
      this.matchSchedulingService.loading$,
      (tournamentConfigLoading: boolean, eventConfigsLoading: boolean, matchCardsLoading: boolean, scheduleGenerating: boolean) => {
        return tournamentConfigLoading || eventConfigsLoading || matchCardsLoading || scheduleGenerating;
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

  private loadTournament(tournamentId: number) {
    const selectedTournamentSelector = createSelector(
      this.tournamentConfigService.selectors.selectEntityMap,
      (entityMap) => {
        return entityMap[tournamentId];
      });
    // tournament information will not change just get it once
    this.tournament$ = this.tournamentConfigService.store.select(selectedTournamentSelector);
    const subscription = this.tournament$.subscribe((tournament: Tournament) => {
      if (!tournament) {
        this.tournamentConfigService.getByKey(tournamentId);
      }
    });
    this.subscriptions.add(subscription);
  }

  private setupMatchCards(tournamentId: number) {
    this.matchCards$ = this.matchCardService.store.select(this.matchCardService.selectors.selectEntities);
    this.loadMatchCardsForDay(1);
  }

  private loadMatchCardsForDay(day: number) {
    this.matchCardService.loadAllForTheTournamentDay(this.tournamentId, day);
  }

  onDayChangedEvent(day: number) {
    this.loadMatchCardsForDay(day);
  }

  /**
   * Generates schedule for the specified day
   * @param day day to generate for
   */
  onGenerateForDay(day: number) {
    const subscription = this.matchSchedulingService.generateScheduleForTournamentAndDay(this.tournamentId, day)
      .pipe(first())
      .subscribe(
        (matchCards: MatchCard[]) => {
          this.matchCardService.putIntoCache(matchCards);
        }, (error: any) => {
          console.log('error ', error);
          const message = error.error?.message ?? error.message;
          this.errorMessagePopupService.showError(message);
        });
    this.subscriptions.add(subscription);
  }

  onFixUnscheduledEvents(event: any) {
    const matchCardIds: number[] = event.matchCardIds;
    const day: number = event.day;
    const subscription = this.matchSchedulingService.generateScheduleForMatchCards(this.tournamentId, day, matchCardIds)
      .pipe(first())
      .subscribe(
        (matchCards: MatchCard[]) => {
          this.matchCardService.putIntoCache(matchCards);
        }, (error: any) => {
          console.log('error ', error);
          const message = error.error?.message ?? error.message;
          this.errorMessagePopupService.showError(message);
        });
    this.subscriptions.add(subscription);
  }

  private loadTournamentEvents(tournamentId: number) {
    this.tournamentEvents$ = this.tournamentEventConfigService.store.select(
      this.tournamentEventConfigService.selectors.selectEntities);
    // load them - they will surface via this selector
    this.tournamentEventConfigService.loadTournamentEvents(this.tournamentId);
  }

  onUpdateMatchCards(updatedMatchCards: MatchCard[]) {
    // console.log('onUpdateMatchCards matchCards', updatedMatchCards);
    this.matchSchedulingService.updateMatchCards(updatedMatchCards)
      .subscribe(() => {
        console.log('finished updating');
      }, (error) => {
        console.log('Error during updating', error);
      });
  }
}
