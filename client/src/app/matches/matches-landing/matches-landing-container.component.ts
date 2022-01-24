import {Component, OnDestroy, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {Observable, Subscription} from 'rxjs';
import {map} from 'rxjs/operators';

import {TournamentConfigService} from '../../tournament/tournament-config/tournament-config.service';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {Tournament} from '../../tournament/tournament-config/tournament.model';
import {AuthenticationService} from '../../user/authentication.service';
import {DateUtils} from '../../shared/date-utils';
import {TodayService} from '../../shared/today.service';

@Component({
  selector: 'app-matches-landing-container',
  template: `
    <app-matches-landing [tournaments]="tournaments$ | async">
    </app-matches-landing>
  `,
  styles: [
  ]
})
export class MatchesLandingContainerComponent implements OnInit, OnDestroy {

  private loading$: Observable<boolean>;

  tournaments$: Observable<Tournament[]>;

  private subscriptions: Subscription = new Subscription();

  constructor(private tournamentConfigService: TournamentConfigService,
              private authenticationService: AuthenticationService,
              private router: Router,
              private linearProgressBarService: LinearProgressBarService,
              private todayService: TodayService) {
    this.setupProgressIndicator();
    this.loadTournaments();
  }

  ngOnInit(): void {
    this.tournamentConfigService.getAll();
  }

  ngOnDestroy() {
    this.subscriptions.unsubscribe();
  }

  /**
   *
   * @private
   */
  private setupProgressIndicator() {
    this.loading$ = this.tournamentConfigService.store.select(this.tournamentConfigService.selectors.selectLoading);

    const loadingSubscription = this.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
  }

  private loadTournaments() {
    this.tournaments$ = this.tournamentConfigService.store.select(this.tournamentConfigService.selectors.selectEntities)
      .pipe(map(
        (tournaments: Tournament[]) => {
          const filteredTournaments: Tournament [] = [];
          const today: Date = this.todayService.todaysDate;
          for (const tournament of tournaments) {
              if (new DateUtils().isDateInRange (today, tournament.startDate, tournament.endDate)) {
                filteredTournaments.push(tournament);
              }
          }

          // if there is one tournament today then navigate to this tournament
          // otherwise let user choose which one - should not be happening
          if (filteredTournaments.length === 1) {
            const tournamentId = filteredTournaments[0].id;
            // navigate directly to today's tournament, pass name so we don't have to retrieve it on the other page
            const extras = {
              state: {
                tournamentName: filteredTournaments[0].name
              }
            };
            this.router.navigateByUrl(`matches/scoreentry/${tournamentId}`, extras);
          } else {
            // sort them so the earlier tournament is listed first
            const dateUtils = new DateUtils();
            filteredTournaments.sort((tournament1, tournament2) => {
              return dateUtils.isDateBefore(tournament1.startDate, tournament2.startDate) ? -1 : 1;
            });
          }

          return filteredTournaments;
        }
        )
      );
  }
}
