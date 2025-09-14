import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChange, SimpleChanges} from '@angular/core';

@Component({
    selector: 'app-monitor-connect',
    templateUrl: './monitor-connect.component.html',
    styleUrls: ['./monitor-connect.component.scss'],
    standalone: false
})
export class MonitorConnectComponent implements OnInit, OnChanges {

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

  // array of tournament tables to choose from
  tournamentTables: number[] = [];

  constructor() {
  }

  ngOnInit(): void {
  }

  onConnect(tableNumber: number) {
    this.performAction('connect', tableNumber);
  }

  onDisconnect(tableNumber: number) {
    this.performAction('disconnect', tableNumber);
  }

  private performAction(action: string, tableNumber: number) {
    const message = {
      action: action,
      tournamentId: this.tournamentId,
      tableToMonitor: tableNumber
    };
    this.connectDisconnect.next(message);
  }

  ngOnChanges(changes: SimpleChanges): void {
    const tournamentsChanges: SimpleChange = changes.tournaments;
    if (tournamentsChanges != null) {
      const tournamentInfos = tournamentsChanges.currentValue;
      if (tournamentInfos != null) {
        if (tournamentInfos.length === 1) {
          const tournamentInfo = tournamentInfos[0];
          this.tournamentId = tournamentInfo.id;
          this.tournamentTables = this.makeArrayOfTournamentTables(tournamentInfo);
        }
      }
    }
  }

  onChangeTournament(strTournamentId: string) {
    if (this.tournaments != null && strTournamentId !== '') {
      const tournamentId = Number(strTournamentId);
      for (let i = 0; i < this.tournaments.length; i++) {
        const tournamentInfo = this.tournaments[i];
        if (tournamentInfo.id === tournamentId) {
          this.tournamentId = tournamentInfo.id;
          this.maxTableNum = tournamentInfo.numberOfTables;
          this.tournamentTables = this.makeArrayOfTournamentTables(tournamentInfo);
        }
      }
    }
  }

  private makeArrayOfTournamentTables(tournamentInfo: any): number [] {
    const monitoredTables: string = tournamentInfo.monitoredTables;
    const strTableNumbers: string [] = monitoredTables.split(',');
    const tournamentTables = [];
    for (let j = 0; j < strTableNumbers.length; j++) {
      const strTableNumber = strTableNumbers[j];
      tournamentTables.push(Number(strTableNumber));
    }
    return tournamentTables;
  }

}
