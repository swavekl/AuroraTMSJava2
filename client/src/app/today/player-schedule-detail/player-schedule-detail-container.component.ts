import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {Observable, Subscription} from 'rxjs';
import {PlayerScheduleService} from '../service/player-schedule.service';
import {PlayerScheduleItem} from '../model/player-schedule-item.model';

@Component({
  selector: 'app-player-schedule-detail-container',
  template: `
    <app-player-schedule-detail [playerScheduleItem]="playerScheduleItem$ | async"
    [returnUrl]="returnUrl">
    </app-player-schedule-detail>
  `,
  styles: [
  ]
})
export class PlayerScheduleDetailContainerComponent implements OnInit, OnDestroy {

  public playerScheduleItem$: Observable<PlayerScheduleItem>;

  public returnUrl: string;

  private subscriptions: Subscription = new Subscription();

  constructor(private activatedRoute: ActivatedRoute,
              private linearProgressBarService: LinearProgressBarService,
              private playerScheduleService: PlayerScheduleService) {
    const matchCardId = this.activatedRoute.snapshot.params['matchCardId'] || 0;
    this.returnUrl = history.state?.returnUrl || '/home';
    this.setupProgressIndicator();
    this.loadPlayerScheduleDetail(matchCardId);
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  private setupProgressIndicator() {
    const subscription = this.playerScheduleService.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(subscription);
  }

  private loadPlayerScheduleDetail(matchCardId: number) {
    this.playerScheduleItem$ = this.playerScheduleService.getPlayerScheduleDetail(matchCardId);
  }
}
