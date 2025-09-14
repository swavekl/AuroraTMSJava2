import {Component, OnDestroy, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {Observable, Subscription} from 'rxjs';
import {map} from 'rxjs/operators';
import moment from 'moment';
import {DateUtils} from '../../shared/date-utils';
import {UserRoles} from '../../user/user-roles.enum';
import {AuthenticationService} from '../../user/authentication.service';
import {Tournament} from '../../tournament/tournament-config/tournament.model';
import {TournamentConfigService} from '../../tournament/tournament-config/tournament-config.service';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {MonitorService} from '../service/monitor.service';
import {TodayService} from '../../shared/today.service';

@Component({
    selector: 'app-monitor-connect-container',
    template: `
    <app-monitor-connect [isConnected]="isConnected$ | async"
                         [tournaments]="monitorTournaments$ | async"
                         (connectDisconnect)="onConnectDisconnect($event)"
    >
    </app-monitor-connect>
  `,
    styles: []
    // providers: [MonitorService]
    ,
    standalone: false
})
export class MonitorConnectContainerComponent implements OnInit, OnDestroy {
  isConnected$: Observable<boolean>;

  monitorTournaments$: Observable<any[]>;

  // tournament id at which to monitor a match
  tournamentId: number;

  // table number at this tournament to monitor
  tableToMonitor: number;

  private subscriptions: Subscription = new Subscription();

  constructor(private monitorService: MonitorService,
              private router: Router,
              private linearProgressBarService: LinearProgressBarService,
              private authenticationService: AuthenticationService,
              private tournamentConfigService: TournamentConfigService,
              private todayService: TodayService) {
    this.setupProgressIndicator();
    this.loadTournamentsForThisMonitor();
  }

  private setupProgressIndicator() {
    const subscription = this.tournamentConfigService.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });

    this.subscriptions.add(subscription);
  }

  /**
   * Load tournaments for monitor
   * @private
   */
  private loadTournamentsForThisMonitor() {
    const monitorProfileId = this.authenticationService.getCurrentUserProfileId();
    this.monitorTournaments$ = this.tournamentConfigService.store.select(
      this.tournamentConfigService.selectors.selectEntities)
      .pipe(
        map((tournaments: Tournament[]) => {
            console.log(`Got ${tournaments.length} tournaments for monitor `, tournaments);
            const filteredTournamentChoices: any [] = [];
            const dateUtils = new DateUtils();
            const today: Date = this.todayService.todaysDate;
            for (const tournament of tournaments) {
              // allow setup of up to a few days before the tournament
              const daysBefore = 100;
              const mBeforeTournamentsDate = moment(tournament.startDate).subtract(daysBefore, 'days').toDate();
              // console.log('mBeforeTournamentsDate', mBeforeTournamentsDate);
              if (dateUtils.isDateInRange(today, mBeforeTournamentsDate, tournament.endDate)) {
                const personnelList = tournament.configuration.personnelList;
                for (const personnel of personnelList) {
                  if (personnel.profileId === monitorProfileId &&
                    personnel.role === UserRoles.ROLE_MONITORS) {
                    filteredTournamentChoices.push({
                      id: tournament.id,
                      name: tournament.name,
                      monitoredTables: tournament.configuration.monitoredTables,
                      numberOfTables: tournament.configuration.numberOfTables
                    });
                    break;
                  }
                }
              }
            }

            return filteredTournamentChoices;
          }
        )
      );
    this.tournamentConfigService.getAll();
  }

  ngOnInit(): void {
    this.isConnected$ = this.monitorService.isConnected();
    const subscription = this.isConnected$
      .subscribe((connected) => {
        console.log('got connected status', connected);
        if (connected === true) {
          if (this.tournamentId && this.tableToMonitor) {
            const url = `/ui/monitor/display/${this.tournamentId}/${this.tableToMonitor}`;
            console.log('connected navigating to table display', url);
            this.router.navigateByUrl(url);
          }
        }
      });
    this.subscriptions.add(subscription);
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  onConnectDisconnect(message: any) {
    if (message?.action === 'connect') {
      this.tournamentId = message.tournamentId;
      this.tableToMonitor = message.tableToMonitor;
      this.monitorService.connect(message.tournamentId, message.tableToMonitor, true);
    } else if (message?.action === 'disconnect') {
      this.monitorService.disconnect();
    }
  }
}
