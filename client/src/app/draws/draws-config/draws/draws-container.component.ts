import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {combineLatest, Observable, of, Subscription} from 'rxjs';
import {first, switchMap} from 'rxjs/operators';
import {createSelector} from '@ngrx/store';
import {TournamentConfigService} from '../../../tournament/tournament-config/tournament-config.service';
import {Tournament} from '../../../tournament/tournament-config/tournament.model';
import {TournamentEventConfigService} from '../../../tournament/tournament-config/tournament-event-config.service';
import {TournamentEvent} from '../../../tournament/tournament-config/tournament-event.model';
import {LinearProgressBarService} from '../../../shared/linear-progress-bar/linear-progress-bar.service';
import {DrawAction, DrawActionType} from './draw-action';
import {DrawService} from '../../draws-common/service/draw.service';
import {DrawItem} from '../../draws-common/model/draw-item.model';
import {DrawType} from '../../draws-common/model/draw-type.enum';
import {MatchCardPrinterService} from '../../../matches/service/match-card-printer.service';
import {MatchCardService} from '../../../matches/service/match-card.service';
import {MatchCard} from '../../../matches/model/match-card.model';

@Component({
  selector: 'app-draws-container',
  template: `
      <app-draws [tournamentEvents]="tournamentEvents$ | async"
                 [draws]="draws$ | async"
                 [tournamentName]="tournamentName$ | async"
                 (drawsAction)="onDrawsAction($event)">
      </app-draws>
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
              private linearProgressBarService: LinearProgressBarService,
              private matchCardPrinterService: MatchCardPrinterService,
              private matchCardService: MatchCardService) {
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
      this.matchCardService.store.select(this.matchCardService.selectors.selectLoading),
      this.matchCardPrinterService.loading$,
      (eventConfigsLoading: boolean, tournamentLoading, drawsLoading: boolean, matchCardsLoading: boolean, printingMatchCards: boolean) => {
        return eventConfigsLoading || tournamentLoading || drawsLoading || matchCardsLoading || printingMatchCards;
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
        this.onLoadDraw(drawAction.eventId, drawAction.payload?.drawType);
        break;
      case DrawActionType.DRAW_ACTION_GENERATE:
        this.onGenerateDraw(drawAction.eventId, drawAction.payload?.drawType);
        break;
      case DrawActionType.DRAW_ACTION_CLEAR:
        this.onClearDraw(drawAction.eventId);
        break;
      case DrawActionType.DRAW_ACTION_UPDATE:
        this.onUpdateDraw(drawAction.eventId, drawAction.payload.movedDrawItems, drawAction.payload.drawType);
        break;
      case DrawActionType.DRAW_ACTION_PRINT:
        this.onPrintMatchCards(drawAction.eventId, drawAction.payload?.drawType);
        break;
    }
  }

  /**
   *
   * @param eventId
   * @param drawType
   * @private
   */
  private onLoadDraw(eventId: number, drawType: DrawType) {
    this.drawService.loadForEvent(eventId, (drawType != null)? drawType : DrawType.ROUND_ROBIN);
  }

  /**
   *
   * @param eventId
   * @param drawType
   * @private
   */
  private onGenerateDraw(eventId: number, drawType: DrawType) {
    this.drawService.generate(eventId, (drawType != null) ? drawType : DrawType.ROUND_ROBIN)
      .pipe(first())
      .subscribe(
        (draws: DrawItem[]) => {
          // console.log(`generated ${drawType} draw for event ${eventId} got draws of length ${draws?.length}`);
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
        // console.log('deleted draw ' + next);
        this.updateEventFlag(eventId);
      });
  }

  /**
   * Updates draw item
   * @param eventId
   * @param drawItems
   * @param drawType
   * @private
   */
  private onUpdateDraw(eventId: number, drawItems: DrawItem [], drawType: DrawType) {
    if (drawItems) {
      this.drawService.updateDrawItems(drawItems)
        .pipe(first())
        .subscribe(() => {
          this.updateEventFlag(eventId);
          this.onLoadDraw(eventId, drawType);
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

  private onPrintMatchCards(eventId: number, drawType: DrawType) {
    let subscription = this.matchCardService.loadForEvent(eventId, true)
      .pipe(
        switchMap((matchCards: MatchCard[]) => {
          const roundMatchCards: number [] = [];
          for (let i = 0; i < matchCards.length; i++) {
            if (matchCards[i].drawType === drawType) {
                roundMatchCards.push(matchCards[i].id);
            }
          }
          this.matchCardPrinterService.downloadAndPrint(this.tournamentId, eventId, roundMatchCards);
          return roundMatchCards;
        }),
        first())
      .subscribe();
    this.subscriptions.add(subscription);
  }
}
