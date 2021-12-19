import { Component, OnInit } from '@angular/core';
import {MonitorService} from '../service/monitor.service';
import {MonitorMessage} from '../model/monitor-message.model';
import {Observable} from 'rxjs';

@Component({
  selector: 'app-monitor-display-container',
  template: `
    <app-monitor-display [matchData]="matchData$ | async">Works</app-monitor-display>
    `,
  styles: [
  ]
})
export class MonitorDisplayContainerComponent implements OnInit {
  matchData$: Observable<MonitorMessage>;

  constructor(private monitorService: MonitorService) { }

  ngOnInit(): void {
    this.matchData$ = this.monitorService.messagesSubject$;
  }

}
