import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {Observable, Subscription} from 'rxjs';
import {MatchSchedulingService} from '../../scheduling/service/match-scheduling.service';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {MatchCard} from '../../matches/model/match-card.model';
import {MonitorService} from '../../monitor/service/monitor.service';

@Component({
  selector: 'app-score-board-container',
  template: `
    <app-score-board [matchCards]="matchCards$ | async"
                     [tournamentId]="tournamentId"
                     [tableNumber]="tableNumber">
    </app-score-board>
  `,
  styles: []
})
export class ScoreBoardContainerComponent implements OnInit {

  // tournament id at which to umpire matches
  tournamentId: number;

  // day of the tournament that is today
  tournamentDay: number;

  // table number at this tournament to umpire at
  tableNumber: number;

  // match cards for matches to be played at this table today
  matchCards$: Observable<MatchCard []>;

  private subscriptions: Subscription = new Subscription();

  constructor(private router: Router,
              private activatedRoute: ActivatedRoute,
              private linearProgressBarService: LinearProgressBarService,
              private matchSchedulingService: MatchSchedulingService,
              private monitorService: MonitorService) {
    const strTournamentId = this.activatedRoute.snapshot.params['tournamentId'] || 0;
    this.tournamentId = Number(strTournamentId);
    const strTournamentDay = this.activatedRoute.snapshot.params['tournamentDay'] || 1;
    this.tournamentDay = Number(strTournamentDay);
    const strTableToMonitor = this.activatedRoute.snapshot.params['tableNumber'] || 1;
    this.tableNumber = Number(strTableToMonitor);

    this.setupProgressIndicator();
    this.loadMatchesForTable();
    this.connectToSocket();
  }

  private setupProgressIndicator() {
    const subscription = this.matchSchedulingService.loading$.subscribe((loading: boolean) => {
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

  private connectToSocket() {
    console.log('Connecting to socket');
    this.monitorService.connect(this.tournamentId, this.tableNumber, false);
  }
}
