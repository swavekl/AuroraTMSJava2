import {Component, OnDestroy, OnInit} from '@angular/core';
import {combineLatest, interval, Observable, of, Subscription} from 'rxjs';
import {first, map} from 'rxjs/operators';
import {TableUsage} from '../model/table-usage.model';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {TableUsageService} from '../service/table-usage.service';
import {TournamentConfigService} from '../../tournament/tournament-config/tournament-config.service';
import {Tournament} from '../../tournament/tournament-config/tournament.model';
import {TodayService} from '../../shared/today.service';
import {MatchCardService} from '../../matches/service/match-card.service';
import {MatchCard} from '../../matches/model/match-card.model';
import {DateUtils} from '../../shared/date-utils';
import {TournamentEventConfigService} from '../../tournament/tournament-config/tournament-event-config.service';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {MatchCardPrinterService} from '../../matches/service/match-card-printer.service';
import {MatchCardPlayabilityStatus, MatchInfo} from '../model/match-info.model';
import {MatchCardStatusUtil} from '../util/match-card-status-util';

@Component({
  selector: 'app-table-usage-container',
  template: `
    <app-table-usage [tableUsageList]="tableUsageList$ | async"
                     [allTodaysMatchCards]="matchCards$ | async"
                     [matchesToPlayInfos]="matchesToPlayInfos$ | async"
                     [tournamentEvents]="tournamentEvents$ | async"
                     (printMatchCards)="onPrintMatchCards($event)"
                     (startMatches)="onStartMatches($event)"
                     (refreshUsage)="onRefresh($event)">
    </app-table-usage>
  `,
  styles: []
})
export class TableUsageContainerComponent implements OnInit, OnDestroy {

  tableUsageList$: Observable<TableUsage[]>;

  matchCards$: Observable<MatchCard[]>;

  matchesToPlayInfos$: Observable<MatchInfo[]>;

  tournamentEvents$: Observable<TournamentEvent[]>;

  private tournamentId: number;

  private tournamentDay: number;

  private subscriptions: Subscription = new Subscription();
  private loading$: Observable<boolean>;

  constructor(private tableUsageService: TableUsageService,
              private tournamentService: TournamentConfigService,
              private todayService: TodayService,
              private matchCardService: MatchCardService,
              private tournamentEventConfigService: TournamentEventConfigService,
              private matchCardPrinterService: MatchCardPrinterService,
              private linearProgressBarService: LinearProgressBarService) {
    this.setupProgressIndicator();
    this.loadTableUsagesForTodaysTournament();
    this.setupAutoRefresh();
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  private setupProgressIndicator() {
    this.loading$ = combineLatest(
      this.tournamentService.store.select(this.tournamentService.selectors.selectLoading),
      this.tableUsageService.store.select(this.tableUsageService.selectors.selectLoading),
      this.tournamentEventConfigService.store.select(this.tournamentEventConfigService.selectors.selectLoading),
      this.matchCardService.store.select(this.matchCardService.selectors.selectLoading),
      this.matchCardPrinterService.loading$,
      (tournamentsLoading: boolean, tableUsageLoading: boolean, eventConfigsLoading: boolean, matchCardsLoading: boolean, matchCardPrinting: boolean) => {
        return tournamentsLoading || tableUsageLoading || eventConfigsLoading || matchCardsLoading || matchCardPrinting;
      }
    );

    const loadingSubscription = this.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
  }

  ngOnInit(): void {
  }

  private loadTableUsagesForTodaysTournament() {
    const todaysDate: Date = this.todayService.todaysDate;
    const subscription = this.tournamentService.getWithQuery(`today=${todaysDate}`)
      .pipe(
        first(),
        map((todaysTournaments: Tournament[]) => {
          if (todaysTournaments?.length > 0) {
            const tournament: Tournament = todaysTournaments[0];
            const difference = new DateUtils().daysBetweenDates(tournament.startDate, todaysDate);
            this.tournamentDay = difference + 1;
            this.tournamentId = tournament.id;
            this.loadEvents(this.tournamentId);
            this.loadTableUsage(this.tournamentId);
            this.loadMatchCards();
            this.filterMatchCardsToPlay();
          }
        }))
      .subscribe();
    this.subscriptions.add(subscription);
  }

  private loadTableUsage(tournamentId: number) {
    this.tableUsageList$ = this.tableUsageService.store.select(
      this.tableUsageService.selectors.selectEntities)
      .pipe(
        map((tableUsages: TableUsage[]) => {
          // console.log('CONTAINER got table usage list ', tableUsages);
          return JSON.parse(JSON.stringify(tableUsages));
        }));
    const params = `tournamentId=${tournamentId}`;
    // no need to subscribe since it is subscribed in a template
    this.tableUsageService.getWithQuery(params);
  }

  /**
   * Need event names
   * @param tournamentId
   * @private
   */
  private loadEvents(tournamentId: number) {
    // this selector will NOT be subscribed by template.
    // It exists only to first check if the events were loaded already and if they were it will stop,
    // if not it will initiate a load and then replace above observable with a new one.
    const localTournamentEvents$ = this.tournamentEventConfigService.store.select(
      this.tournamentEventConfigService.selectors.selectEntities);
    const subscription = localTournamentEvents$
      .pipe(first())
      .subscribe(
        (events: TournamentEvent[]) => {
          // check if has events for this tournament
          let hasEvents = false;
          if (events != null && events.length > 0) {
            for (const event of events) {
              if (event.tournamentFk === this.tournamentId) {
                hasEvents = true;
                break;
              }
            }
          }
          if (hasEvents) {
            this.tournamentEvents$ = of(events);
          } else {
            // don't have event configs cached - load them - again template is subscribed to this
            this.tournamentEvents$ = this.tournamentEventConfigService.loadTournamentEvents(this.tournamentId);
          }
        },
        (error: any) => {
          console.log('error loading tournament events ' + JSON.stringify(error));
        }
      );

    this.subscriptions.add(subscription);
  }

  /**
   *
   * @private
   */
  private loadMatchCards() {
    this.matchCards$ = this.matchCardService.store.select(this.matchCardService.selectors.selectEntities);
    // .pipe(
    //   tap((matchCards) => {
    //     console.log('CONTAINER got match cards', matchCards);
    //   }));
    this.matchCardService.loadAllForTheTournamentDay(this.tournamentId, this.tournamentDay, true);
  }

  /**
   * Prepares match infos for matches thar are still to be played which combines match card and event information
   * @private
   */
  private filterMatchCardsToPlay() {
    this.matchesToPlayInfos$ = combineLatest(this.tableUsageList$, this.matchCards$, this.tournamentEvents$,
      (tableUsageList: TableUsage[], matchCards: MatchCard[], tournamentEvents: TournamentEvent[]) => {
        return new MatchCardStatusUtil().generateMatchInfos(tableUsageList, matchCards, tournamentEvents);
      });
  }

  /**
   * Print selected match cards
   * @param printInfo
   */
  public onPrintMatchCards(printInfo: any) {
    const matchCardIds: number [] = printInfo.matchCardIds;
    const eventId = printInfo.eventId;
    const tournamentId = this.tournamentId;
    this.matchCardPrinterService.downloadAndPrint(tournamentId, eventId, matchCardIds);
  }

  /**
   * Start selected matches on tables
   * @param tableUsages
   */
  onStartMatches(tableUsages: TableUsage[]) {
    this.tableUsageService.updateMany(tableUsages)
      .pipe(first())
      .subscribe((updated: TableUsage[]) => {
        this.tableUsageService.updateManyInCache(updated);
      });
  }

  /**
   * Manual refresh was requested
   * @param $event
   */
  onRefresh($event: any) {
    this.matchCardService.clearCache();
    this.tableUsageService.clearCache();
    this.matchCardService.loadAllForTheTournamentDay(this.tournamentId, this.tournamentDay, true);
    const params = `tournamentId=${this.tournamentId}`;
    this.tableUsageService.getWithQuery(params);
  }

  /**
   * setup automatic refresh every minute so screen has up to date data on usage and match completion
   * @private
   */
  private setupAutoRefresh() {
    // setup automatic refresh every minute
    const subscription = interval(60 * 1000).subscribe(n => {
      this.onRefresh('');
    });
    this.subscriptions.add(subscription);
  }

}
