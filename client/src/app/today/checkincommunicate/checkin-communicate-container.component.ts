import {Component, OnDestroy, OnInit} from '@angular/core';
import {Observable, of, Subscription} from 'rxjs';
import {PlayerStatus} from '../model/player-status.model';
import {ActivatedRoute, Router} from '@angular/router';
import {PlayerStatusService} from '../service/player-status.service';
import {AuthenticationService} from '../../user/authentication.service';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {first, map} from 'rxjs/operators';
import {TodayService} from '../../shared/today.service';

@Component({
  selector: 'app-checkin-communicate-container-component',
  template: `
    <app-checkincommunicate [playerStatus]="playerStatus$ | async"
                            [eventName]="eventName"
                            [tournamentDay]="tournamentDay"
                            (saved)="onPlayerStatusSaved($event)"
                            (canceled)="onPlayerStatusCanceled($event)"
    ></app-checkincommunicate>
  `,
  styles: []
})
export class CheckinCommunicateContainerComponent implements OnInit, OnDestroy {

  playerStatus$: Observable<PlayerStatus>;
  private subscriptions: Subscription = new Subscription();
  private eventId: number;
  public tournamentDay: number;
  private tournamentId: number;
  public eventName: string;

  constructor(private router: Router,
              private activatedRoute: ActivatedRoute,
              private playerStatusService: PlayerStatusService,
              private authenticationService: AuthenticationService,
              private todayService: TodayService,
              private linearProgressBarService: LinearProgressBarService) {
    this.eventId = this.activatedRoute.snapshot.params['eventId'] || 0;
    this.eventName = history?.state?.eventName || '';
    this.tournamentId = this.activatedRoute.snapshot.params['tournamentId'] || 0;
    this.tournamentDay = this.activatedRoute.snapshot.params['tournamentDay'] || 1;
    this.setupProgressIndicator();
    this.loadPlayerStatus();
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  /**
   *
   * @private
   */
  private setupProgressIndicator() {
    const loadingSubscription = this.playerStatusService.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
  }


  /**
   *
   * @private
   */
  private loadPlayerStatus() {
    const profileId = this.authenticationService.getCurrentUserProfileId();
    let params = `tournamentId=${this.tournamentId}&tournamentDay=${this.tournamentDay}&playerProfileId=${profileId}`;
    if (this.eventId !== 0) {
      params += `&eventId=${this.eventId}`;
    }
    const subscription: Subscription = this.playerStatusService.getWithQuery(params)
      .pipe(
        first(),
        map(
          (playerStatusList: PlayerStatus[]) => {
            // console.log('got player status list', playerStatusList);
            if (playerStatusList.length > 0) {
              this.playerStatus$ = of(playerStatusList[0]);
            } else {
              // create new player status
              const playerStatus = new PlayerStatus();
              playerStatus.playerProfileId = profileId;
              playerStatus.tournamentId = this.tournamentId;
              playerStatus.tournamentDay = this.tournamentDay;
              playerStatus.eventId = this.eventId;
              // console.log('creating new player profile');
              this.playerStatus$ = of(playerStatus);
            }
          })
      ).subscribe();
    this.subscriptions.add(subscription);
  }

  onPlayerStatusSaved(playerStatus: PlayerStatus) {
    console.log('updating playerStatus', playerStatus);
    if (playerStatus != null) {
      this.playerStatusService.upsert(playerStatus)
        .pipe(first())
        .subscribe(
          next => {
            console.log('Saved player status ', next);
            if (next) {
              this.navigateBack();
            }
          }
        );
    }
  }

  onPlayerStatusCanceled($event: any) {
    this.navigateBack();
  }

  navigateBack() {
    // console.log('navigating back to ', this.todayService.todayUrl);
    // this.router.navigateByUrl(this.todayService.todayUrl);
    this.router.navigateByUrl('/ui/home');
  }

}
