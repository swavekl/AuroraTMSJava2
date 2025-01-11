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
import {Match} from '../../../matches/model/match.model';
import {ConfirmationPopupComponent} from '../../../shared/confirmation-popup/confirmation-popup.component';
import {MatDialog} from '@angular/material/dialog';
import {PlayerStatusService} from '../../../today/service/player-status.service';
import {PlayerStatus} from '../../../today/model/player-status.model';
import {CheckInType} from '../../../tournament/model/check-in-type.enum';
import {MatchCardInfoService} from '../../../matches/service/match-card-info.service';
import {MatchCardInfo} from '../../../matches/model/match-card-info.model';
import {ReplacePlayerRequest} from '../reaplace-player-popup/replace-player-popup.component';
import {ErrorMessagePopupService} from '../../../shared/error-message-dialog/error-message-popup.service';

@Component({
  selector: 'app-draws-container',
  template: `
      <app-draws [tournamentEvents]="tournamentEvents$ | async"
                 [draws]="draws$ | async"
                 [playerStatusList]="playerStatusList$ | async"
                 [matchCardInfos]="matchCardInfos$ | async"
                 [tournamentName]="tournamentName$ | async"
                 [returnUrl]="returnUrl"
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

  // match card information i.e. without matches
  matchCardInfos$: Observable<MatchCardInfo[]>

  // check in statuses of all players
  playerStatusList$: Observable<PlayerStatus[]>;

  tournamentName$: Observable<string>;

  returnUrl: string;

  private tournamentId: number;

  private loading$: Observable<boolean>;

  private subscriptions: Subscription = new Subscription();

  private isDailyCheckin: boolean = true;


  constructor(private tournamentEventConfigService: TournamentEventConfigService,
              private tournamentConfigService: TournamentConfigService,
              private activatedRoute: ActivatedRoute,
              private drawService: DrawService,
              private linearProgressBarService: LinearProgressBarService,
              private matchCardPrinterService: MatchCardPrinterService,
              private matchCardService: MatchCardService,
              private matchCardInfoService: MatchCardInfoService,
              private playerStatusService: PlayerStatusService,
              private errorMessagePopupService: ErrorMessagePopupService,
              private dialog: MatDialog) {
    const strTournamentId = this.activatedRoute.snapshot.params['tournamentId'] || 0;
    this.tournamentId = Number(strTournamentId);
    this.returnUrl = (history?.state?.returnUrl) ? history?.state?.returnUrl : '/ui/tournamentsconfig';
    this.setupProgressIndicator();
    this.loadTournamentEvents(this.tournamentId);
    this.loadTournamentName(this.tournamentId);
    this.setupDraws();
    this.setupMatchCards();
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
      this.matchCardInfoService.loading$,
      this.playerStatusService.store.select(this.playerStatusService.selectors.selectLoading),
      (eventConfigsLoading: boolean, tournamentLoading, drawsLoading: boolean, matchCardsLoading: boolean,
       printingMatchCards: boolean, playerStatusLoading: boolean, matchCardInfosLoading: boolean) => {
        return eventConfigsLoading || tournamentLoading || drawsLoading || matchCardsLoading
          || printingMatchCards || playerStatusLoading || matchCardInfosLoading;
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
        this.isDailyCheckin = tournament.configuration?.checkInType == CheckInType.DAILY;
      }
    });
    this.subscriptions.add(subscription);
  }

  private setupDraws() {
    // this will be subscribed by the template
    this.drawService.clearCache();
    this.draws$ = this.drawService.entities$;
  }

  private setupMatchCards() {
    this.matchCardService.clearCache();
  }

  /**
   * Entry point for actions initiated by the child component
   * @param drawAction
   */
  onDrawsAction(drawAction: DrawAction) {
    switch (drawAction.actionType) {
      case DrawActionType.DRAW_ACTION_LOAD:
        this.onLoadDraw(drawAction.eventId, drawAction.payload?.drawType);
        if (drawAction.payload?.loadStatus) {
          this.onLoadStatus(drawAction.eventId, drawAction.payload?.tournamentDay);
        }
        this.onLoadMatchCardInfos(drawAction.eventId, drawAction.payload?.drawType);
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
      case DrawActionType.DRAW_ACTION_LOAD_STATUS:
        this.onLoadStatus(drawAction.eventId, drawAction.payload?.tournamentDay);
        break;
      case DrawActionType.DRAW_ACTION_REPLACE_PLAYER:
        this.onReplacePlayer(drawAction.eventId, drawAction.payload);
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

  private onLoadMatchCardInfos(eventId: number, drawType: DrawType) {
    this.matchCardInfos$ = this.matchCardInfoService.load(eventId);
  }

  private onPrintMatchCards(eventId: number, drawType: DrawType) {
    let subscription = this.matchCardService.loadForEvent(eventId, true)
      .pipe(
        switchMap((matchCards: MatchCard[]) => {
          // for single elimination match cards just print the first round for now
          let maxRoundNum = 0;
          if (drawType === DrawType.SINGLE_ELIMINATION) {
            matchCards.forEach((matchCard: MatchCard) => {
              maxRoundNum = Math.max(matchCard.round, maxRoundNum);
            });
          }

          const matchCardIdsToPrint: number [] = [];
          let tablesAssigned: boolean = true;
          matchCards.forEach((matchCard: MatchCard) => {
            if (matchCard.drawType === drawType) {
              if (matchCard.drawType === DrawType.ROUND_ROBIN) {
                matchCardIdsToPrint.push(matchCard.id);
              } else {
                if (matchCard.round === maxRoundNum) {
                  const match = matchCard.matches[0];
                  // skip match cards for bye matches - or those from other rounds
                  if (match.playerAProfileId !== Match.TBD_PROFILE_ID &&
                    match.playerBProfileId !== Match.TBD_PROFILE_ID) {
                    matchCardIdsToPrint.push(matchCard.id);
                  }
                }
              }

              // find out if any matches don't have a scheduled table
              if (matchCard.assignedTables == null) {
                tablesAssigned = false;
              }
            }
          });

          if (tablesAssigned) {
            this.matchCardPrinterService.downloadAndPrint(this.tournamentId, eventId, matchCardIdsToPrint);
          } else {
            const message = "Some match cards don't have assigned table numbers. " +
              "You can assign tables on the Schedule screen. " +
              "Press OK to print without table numbers.  Press Cancel to cancel printing.";

            const config = {
              width: '450px', height: '230px', data: {
                message: message, contentAreaHeight: '100px'
              }
            };
            const dialogRef = this.dialog.open(ConfirmationPopupComponent, config);
            dialogRef.afterClosed().subscribe(result => {
              if (result === 'ok') {
                this.matchCardPrinterService.downloadAndPrint(this.tournamentId, eventId, matchCardIdsToPrint);
              }
            });
          }
          return matchCardIdsToPrint;
        }),
        first())
      .subscribe();
    this.subscriptions.add(subscription);
  }

  private onLoadStatus(eventId: number, tournamentDay: number) {
    // this is subscribed by template | async
    this.playerStatusService.clearCache();
    this.playerStatusList$ = this.playerStatusService.entities$;
    let params = `tournamentId=${this.tournamentId}&tournamentDay=${tournamentDay}&eventId=${eventId}&isDailyCheckin=${this.isDailyCheckin}`;
    this.playerStatusService.loadWithQuery(params);
  }

  private onReplacePlayer(eventId: number, payload: any) {
    const replaceRequest: ReplacePlayerRequest = payload.replaceRequest;
    const drawType = replaceRequest.drawItem.drawType;
    this.drawService.replacePlayerInDraw(replaceRequest.drawItem, replaceRequest.playerToAddEntryId, this.tournamentId)
      .pipe(first())
      .subscribe(
        {
          next: (updatedDrawItem: DrawItem) => {
            this.updateEventFlag(eventId);
            this.onLoadDraw(eventId, drawType);
          },
          error: (error: any) => {
            this.errorMessagePopupService.showError(error);
          }
        });
  }
}
