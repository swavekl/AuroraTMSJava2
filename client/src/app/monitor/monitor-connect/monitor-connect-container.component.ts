import {Component, OnDestroy, OnInit} from '@angular/core';
import {MonitorService} from '../service/monitor.service';
import {Observable, Subscription} from 'rxjs';
import {Router} from '@angular/router';
import {first} from 'rxjs/operators';

@Component({
  selector: 'app-monitor-connect-container',
  template: `
    <app-monitor-connect [isConnected]="isConnected$ | async"
                         (connect)="onConnect($event)"
                         (disconnect)="onDisconnect($event)"
    >
    </app-monitor-connect>
  `,
  styles: []
})
export class MonitorConnectContainerComponent implements OnInit, OnDestroy {
  isConnected$: Observable<boolean>;

  tournamentId: number;

  subscriptions: Subscription = new Subscription();

  constructor(private monitorService: MonitorService,
              private router: Router) {
  }

  ngOnInit(): void {
    this.isConnected$ = this.monitorService.isConnected();
    // .pipe(first())
    const subscription = this.isConnected$
      .subscribe((connected) => {
        console.log('connected navigating to table display');
        if (connected === true) {
          this.router.navigateByUrl('/monitor/display');
        }
      });
    this.subscriptions.add(subscription);
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  onConnect($event: any) {
    const topicName = '/topic/monitor';
    this.monitorService.connect(topicName);
  }

  onDisconnect($event: any) {
    this.monitorService.disconnect();
  }
}
