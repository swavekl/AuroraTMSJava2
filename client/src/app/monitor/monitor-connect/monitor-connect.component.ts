import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';

@Component({
  selector: 'app-monitor-connect',
  templateUrl: './monitor-connect.component.html',
  styleUrls: ['./monitor-connect.component.scss']
})
export class MonitorConnectComponent implements OnInit {

  // tournaments to which this monitor has the ability to connect for monitoring
  @Input()
  tournaments: any[];

  // connection status
  @Input()
  isConnected: boolean;

  @Output()
  connectDisconnect: EventEmitter<any> = new EventEmitter<any>();

  // tournament at which to monitor the table
  tournamentId: number;

  // how many tables at this tournament - in practice only showcourts #1, #2 etc are monitored
  maxTableNum: number;

  // selected table number to monitor
  tableToMonitor: number;

  constructor() {
  }

  ngOnInit(): void {
  }

  onConnect($event: MouseEvent) {
    this.performAction('connect');
  }

  onDisconnect($event: MouseEvent) {
    this.performAction('disconnect');
  }

  private performAction(action: string) {
    const message = {
      action: action,
      tournamentId: this.tournamentId,
      tableToMonitor: this.tableToMonitor
    };
    this.connectDisconnect.next(message);
  }

  onChangeTournament(strTournamentId: string) {
    if (this.tournaments != null && strTournamentId !== '') {
      const tournamentId = Number(strTournamentId);
      for (let i = 0; i < this.tournaments.length; i++) {
        const tournamentInfo = this.tournaments[i];
        if (tournamentInfo.id === tournamentId) {
          this.maxTableNum = tournamentInfo.numberOfTables;
        }
      }
    }
  }
}
