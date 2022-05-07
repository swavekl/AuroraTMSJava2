import {Component, OnDestroy, OnInit} from '@angular/core';
import {TournamentEventConfigService} from '../../tournament/tournament-config/tournament-event-config.service';
import {combineLatest, Observable, of, Subscription} from 'rxjs';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {ActivatedRoute} from '@angular/router';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {DrawAction, DrawActionType} from './draw-action';
import {DrawService} from '../service/draw.service';
import {DrawItem} from '../model/draw-item.model';
import {DrawType} from '../model/draw-type.enum';
import {first} from 'rxjs/operators';
import {TournamentInfoService} from '../../tournament/service/tournament-info.service';
import {TournamentConfigService} from '../../tournament/tournament-config/tournament-config.service';
import {createSelector} from '@ngrx/store';
import {Tournament} from '../../tournament/tournament-config/tournament.model';

@Component({
  selector: 'app-draws-container',
  template: `
    <p>
      <app-draws [tournamentEvents]="tournamentEvents$ | async"
                 [draws]="draws$ | async"
                 [tournamentName]="tournamentName$ | async"
                 (drawsAction)="onDrawsAction($event)">
      </app-draws>
    </p>
  `,
  styles: []
})
export class DrawsContainerComponent implements OnInit, OnDestroy {

  // list of tournament events
  tournamentEvents$: Observable<TournamentEvent[]>;

  // draws for the currently selected event
  draws$: Observable<DrawItem[]>;

  tournamentName$: Observable<string>;

  private tournamentId: number;

  private loading$: Observable<boolean>;

  private subscriptions: Subscription = new Subscription();


  constructor(private tournamentEventConfigService: TournamentEventConfigService,
              private tournamentConfigService: TournamentConfigService,
              private activatedRoute: ActivatedRoute,
              private drawService: DrawService,
              private linearProgressBarService: LinearProgressBarService) {
    const strTournamentId = this.activatedRoute.snapshot.params['tournamentId'] || 0;
    this.tournamentId = Number(strTournamentId);
    this.setupProgressIndicator();
    this.loadTournamentEvents(this.tournamentId);
    this.loadTournamentName(this.tournamentId);
    this.setupDraws();
  }

  /**
   *
   * @private
   */
  private setupProgressIndicator() {
    // if any of the service are loading show the loading progress
    this.loading$ = combineLatest(
      this.tournamentEventConfigService.store.select(this.tournamentEventConfigService.selectors.selectLoading),
      this.tournamentConfigService.store.select(this.tournamentConfigService.selectors.selectLoading),
      this.drawService.store.select(this.drawService.selectors.selectLoading),
      (eventConfigsLoading: boolean, tournamentLoading, drawsLoading: boolean) => {
        return eventConfigsLoading || tournamentLoading || drawsLoading;
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

  private loadTournamentEvents(tournamentId: number) {
    this.tournamentEvents$ = this.tournamentEventConfigService.store.select(
      this.tournamentEventConfigService.selectors.selectEntities);
    // load them - they will surface via this selector
    this.tournamentEventConfigService.loadTournamentEvents(tournamentId);
  }

  private loadTournamentName(tournamentId: number) {
    const selectedTournamentSelector = createSelector(
      this.tournamentConfigService.selectors.selectEntityMap,
      (entityMap) => {
        return entityMap[tournamentId];
      });
    const localTournament$ = this.tournamentConfigService.store.select(selectedTournamentSelector);
    const subscription = localTournament$
      .subscribe((tournament: Tournament) => {
      if (!tournament) {
        this.tournamentConfigService.getByKey(tournamentId);
      } else {
        this.tournamentName$ = of(tournament.name);
      }
    });
    this.subscriptions.add(subscription);
  }

  private setupDraws() {
    // this will be subscribed by the template
    this.draws$ = this.drawService.entities$;
  }

  /**
   * Entry point for actions initiated by the child component
   * @param drawAction
   */
  onDrawsAction(drawAction: DrawAction) {
    switch (drawAction.actionType) {
      case DrawActionType.DRAW_ACTION_LOAD:
        this.onLoadDraw(drawAction.eventId);
        break;
      case DrawActionType.DRAW_ACTION_GENERATE:
        this.onGenerateDraw(drawAction.eventId);
        break;
      case DrawActionType.DRAW_ACTION_CLEAR:
        this.onClearDraw(drawAction.eventId);
        break;
      case DrawActionType.DRAW_ACTION_UPDATE:
        this.onUpdateDraw(drawAction.eventId, drawAction.payload.movedDrawItems);
        break;
    }
  }

  /**
   *
   * @param eventId
   * @private
   */
  private onLoadDraw(eventId: number) {
    this.drawService.loadForEvent(eventId, DrawType.ROUND_ROBIN);
  }

  /**
   *
   * @param eventId
   * @private
   */
  private onGenerateDraw(eventId: number) {
    this.drawService.generate(eventId, DrawType.ROUND_ROBIN)
      .pipe(first())
      .subscribe(
        (draws: DrawItem[]) => {
          console.log('generated draw for event ' + eventId + ' got draws of length ' + draws?.length);
          this.updateEventFlag(eventId);
        },
        (error: any) => {
          console.log('error generating draws ' + error);
        });
  }

  /**
   *
   * @param eventId
   * @private
   */
  private onClearDraw(eventId: number) {
    this.drawService.deleteForEvent(eventId)
      .pipe(first())
      .subscribe((next: number) => {
        console.log('deleted draw ' + next);
        this.updateEventFlag(eventId);
      });
  }

  /**
   * Updates draw item
   * @param eventId
   * @param drawItems
   * @private
   */
  private onUpdateDraw(eventId: number, drawItems: DrawItem []) {
    if (drawItems) {
      this.drawService.updateDrawItems(drawItems)
        .pipe(first())
        .subscribe(() => {
          this.updateEventFlag(eventId);
        });
    }
  }

  /**
   * Clears the flag in cached tournament event so we don't have to go to server to fetch it
   * @param eventId
   * @private
   */
  private updateEventFlag (eventId: number) {
    this.tournamentEventConfigService.updateOneInCache({id: eventId, matchScoresEntered: false});
  }
}
