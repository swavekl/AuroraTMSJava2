import {Component, OnDestroy, OnInit} from '@angular/core';
import {MonitorService} from '../service/monitor.service';
import {MonitorMessage} from '../model/monitor-message.model';
import {Observable, Subscription} from 'rxjs';

@Component({
  selector: 'app-monitor-display-container',
  template: `
    <app-monitor-display [matchData]="matchData$ | async"></app-monitor-display>
    `,
  styles: [
  ]
})
export class MonitorDisplayContainerComponent implements OnInit, OnDestroy {
  matchData$: Observable<MonitorMessage>;

  private subscriptions: Subscription = new Subscription();

  constructor(private monitorService: MonitorService) {
  }

  ngOnInit(): void {
    this.matchData$ = this.monitorService.messagesSubject$;
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }
}
