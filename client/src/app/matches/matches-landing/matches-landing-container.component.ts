import {Component, OnDestroy, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {Observable, Subscription} from 'rxjs';
import {map} from 'rxjs/operators';

import {TournamentConfigService} from '../../tournament/tournament-config/tournament-config.service';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {Tournament} from '../../tournament/tournament-config/tournament.model';
import {AuthenticationService} from '../../user/authentication.service';
import {DateUtils} from '../../shared/date-utils';

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
              private linearProgressBarService: LinearProgressBarService) {
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
          console.log('got tournaments for data entry ', tournaments);
          const filteredTournaments: Tournament [] = [];
          const today: Date = new Date();
          for (const tournament of tournaments) {
              // if (new DateUtils().isDateInRange (today, tournament.startDate, tournament.endDate)) {
              //   filteredTournaments.push(tournament);
              // }
              filteredTournaments.push(tournament);
          }

          if (filteredTournaments.length === 1) {
            // navigate directly to today's tournament
            const tournamentId = filteredTournaments[0].id;
            console.log('navigating to tournament score entry ' + tournamentId);
            this.router.navigateByUrl(`matches/scoreentry/${tournamentId}`);
          }

          // return of(filteredTournaments);
          return filteredTournaments;
        }
        )
      );
  }
}
