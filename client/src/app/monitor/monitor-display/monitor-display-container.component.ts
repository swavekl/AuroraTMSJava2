import {Component, OnDestroy, OnInit} from '@angular/core';
import {MonitorService} from '../service/monitor.service';
import {MonitorMessage} from '../model/monitor-message.model';
import {Observable, Subscription} from 'rxjs';
import {ActivatedRoute} from '@angular/router';

@Component({
  selector: 'app-monitor-display-container',
  template: `
    <app-monitor-display [matchData]="matchData$ | async"
                         [isConnected]="connectionStatus$ | async"
                         [tableNumber]="tableNumber"
    ></app-monitor-display>
    `,
  styles: [
  ]
})
export class MonitorDisplayContainerComponent implements OnInit, OnDestroy {
  matchData$: Observable<MonitorMessage>;
  connectionStatus$: Observable<boolean>;

  tournamentId: number;
  tableNumber: number;

  private subscriptions: Subscription = new Subscription();

  constructor(private monitorService: MonitorService,
              private activatedRoute: ActivatedRoute) {
    const strTournamentId = this.activatedRoute.snapshot.params['tournamentId'] || 0;
    this.tournamentId = Number(strTournamentId);
    const strTableToMonitor = this.activatedRoute.snapshot.params['tableNumber'] || 1;
    this.tableNumber = Number(strTableToMonitor);
    console.log (`tournament ${this.tournamentId}, tableToMonitor: ${this.tableNumber}`);
  }

  ngOnInit(): void {
    this.matchData$ = this.monitorService.messagesSubject$;
    this.connectionStatus$ = this.monitorService.isConnected();
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }
}
