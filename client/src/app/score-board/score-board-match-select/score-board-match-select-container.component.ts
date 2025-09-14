import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {combineLatest, Observable, Subscription} from 'rxjs';
import {MatchSchedulingService} from '../../scheduling/service/match-scheduling.service';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {MatchCard} from '../../matches/model/match-card.model';
import {MonitorService} from '../../monitor/service/monitor.service';
import {TournamentEventConfigService} from '../../tournament/tournament-config/tournament-event-config.service';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';

@Component({
    selector: 'app-score-board-container',
    template: `
    <app-score-board [matchCards]="matchCards$ | async"
                     [tournamentEvents]="tournamentEvents$ | async"
                     [tournamentId]="tournamentId"
                     [tableNumber]="tableNumber"
                     [tournamentDay]="tournamentDay">
    </app-score-board>
  `,
    styles: [],
    standalone: false
})
export class ScoreBoardMatchSelectContainerComponent implements OnInit {

  // tournament id at which to umpire matches
  tournamentId: number;

  // day of the tournament that is today
  tournamentDay: number;

  // table number at this tournament to umpire at
  tableNumber: number;

  // match cards for matches to be played at this table today
  matchCards$: Observable<MatchCard []>;

  // tournament event names
  tournamentEvents$: Observable<TournamentEvent[]>;

  private subscriptions: Subscription = new Subscription();
  private loading$: Observable<boolean>;

  constructor(private router: Router,
              private activatedRoute: ActivatedRoute,
              private linearProgressBarService: LinearProgressBarService,
              private matchSchedulingService: MatchSchedulingService,
              private monitorService: MonitorService,
              private tournamentEventConfigService: TournamentEventConfigService) {
    const strTournamentId = this.activatedRoute.snapshot.params['tournamentId'] || 0;
    this.tournamentId = Number(strTournamentId);
    const strTournamentDay = this.activatedRoute.snapshot.params['tournamentDay'] || 1;
    this.tournamentDay = Number(strTournamentDay);
    const strTableToMonitor = this.activatedRoute.snapshot.params['tableNumber'] || 1;
    this.tableNumber = Number(strTableToMonitor);

    this.setupProgressIndicator();
    this.loadMatchesForTable();
    this.loadTournamentEvents(this.tournamentId);
    this.connectToSocket();
  }

  private setupProgressIndicator() {
    // if any of the service are loading show the loading progress
    this.loading$ = combineLatest(
      this.tournamentEventConfigService.store.select(this.tournamentEventConfigService.selectors.selectLoading),
      this.matchSchedulingService.loading$,
      (eventConfigsLoading: boolean, matchListLoading) => {
        return eventConfigsLoading || matchListLoading;
      }
    );

    const subscription = this.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });

    this.subscriptions.add(subscription);
  }

  ngOnInit(): void {
  }

  private loadMatchesForTable() {
    this.matchCards$ = this.matchSchedulingService.getScheduleForTournamentDayAndTable(
      this.tournamentId, this.tournamentDay, this.tableNumber);
  }

  private loadTournamentEvents(tournamentId: number) {
    this.tournamentEvents$ = this.tournamentEventConfigService.store.select(
    this.tournamentEventConfigService.selectors.selectEntities);
    // load them - they will surface via this selector
    this.tournamentEventConfigService.loadTournamentEvents(tournamentId);
  }

  private connectToSocket() {
    console.log('Connecting to socket');
    this.monitorService.connect(this.tournamentId, this.tableNumber, false);
  }
}
