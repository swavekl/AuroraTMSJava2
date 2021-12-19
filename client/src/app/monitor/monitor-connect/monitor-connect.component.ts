import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Observable} from 'rxjs';

@Component({
  selector: 'app-monitor-connect',
  templateUrl: './monitor-connect.component.html',
  styleUrls: ['./monitor-connect.component.scss']
})
export class MonitorConnectComponent implements OnInit {

  @Input()
  isConnected: boolean;

  @Output()
  connect: EventEmitter<any> = new EventEmitter<any>();

  @Output()
  disconnect: EventEmitter<any> = new EventEmitter<any>();

  // tournament at which to monitor the table
  tournamentId: number;

  // how many tables at this tournament - in practice only showcourts #1, #2 etc are monitored
  maxTableNum: number;

  // table number to monitor
  tableToMonitor: number;

  constructor() {
  }

  ngOnInit(): void {
  }

  onConnect($event: MouseEvent) {
    this.connect.next('connect');
  }

  onDisconnect($event: MouseEvent) {
    this.disconnect.next('disconnect');
  }
}
