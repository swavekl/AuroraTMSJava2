import {Component, OnDestroy, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {AuthenticationService} from '../../user/authentication.service';
import {TournamentConfigService} from '../../tournament/tournament-config/tournament-config.service';
import {Observable, Subscription} from 'rxjs';
import {map} from 'rxjs/operators';
import {Tournament} from '../../tournament/tournament-config/tournament.model';
import {DateUtils} from '../../shared/date-utils';
import * as moment from 'moment';
import {UserRoles} from '../../user/user-roles.enum';

@Component({
  selector: 'app-score-board-configure-container',
  template: `
    <app-score-board-configure [tournaments]="tournamentInfos$ | async"
    (tableSelected)="onTableSelected($event)">
    </app-score-board-configure>
  `,
  styles: [
  ]
})
export class ScoreBoardConfigureContainerComponent implements OnInit, OnDestroy {

  tournamentInfos$: Observable<any[]>;

  // tournament id at which to monitor a match
  tournamentId: number;

  // table number at this tournament to monitor
  tableToMonitor: number;

  private subscriptions: Subscription = new Subscription();

  constructor(private router: Router,
              private linearProgressBarService: LinearProgressBarService,
              private authenticationService: AuthenticationService,
              private tournamentConfigService: TournamentConfigService) {
    this.setupProgressIndicator();
    this.loadTournamentsForThisScoreBoard();
  }

  ngOnInit(): void {
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
  private loadTournamentsForThisScoreBoard() {
    const monitorProfileId = this.authenticationService.getCurrentUserProfileId();
    this.tournamentInfos$ = this.tournamentConfigService.store.select(
      this.tournamentConfigService.selectors.selectEntities)
      .pipe(
        map((tournaments: Tournament[]) => {
            console.log(`Got ${tournaments.length} tournaments for monitor `, tournaments);
            const filteredTournamentChoices: any [] = [];
            const dateUtils = new DateUtils();
            const today: Date = new Date();
            for (const tournament of tournaments) {
              // allow setup of up to a few days before the tournament
              const daysBefore = 370;
              const mBeforeTournamentsDate = moment(tournament.startDate).subtract(daysBefore, 'days').toDate();
              if (dateUtils.isDateInRange(today, mBeforeTournamentsDate, tournament.endDate)) {
                const personnelList = tournament.configuration.personnelList;
                for (const personnel of personnelList) {
                  if (personnel.profileId === monitorProfileId &&
                    personnel.role === UserRoles.ROLE_DIGITAL_SCORE_BOARDS) {
                    const difference = new DateUtils().daysBetweenDates(tournament.startDate, today);
                    let tournamentDay = difference + 1;
// todo - cleanup after testing
tournamentDay = 1;
                    filteredTournamentChoices.push({
                      id: tournament.id,
                      name: tournament.name,
                      monitoredTables: tournament.configuration.monitoredTables,
                      numberOfTables: tournament.configuration.numberOfTables,
                      tournamentDay: tournamentDay
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

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  onTableSelected(selectedTableInfo: any) {
    console.log('user selected table', selectedTableInfo);
    const url = `/ui/scoreboard/scoreentry/${selectedTableInfo.tournamentId}/${selectedTableInfo.tournamentDay}/${selectedTableInfo.tableNumber}`;
    this.router.navigateByUrl(url);
  }
}
