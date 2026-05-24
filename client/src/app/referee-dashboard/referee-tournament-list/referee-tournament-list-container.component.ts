import {Component, OnDestroy, OnInit} from '@angular/core';
import {Observable, Subscription} from 'rxjs';

import {TournamentConfigService} from '../../tournament/tournament-config/tournament-config.service';
import {Tournament} from '../../tournament/tournament-config/tournament.model';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {map} from 'rxjs/operators';
import {AuthenticationService} from '../../user/authentication.service';
import {UserRoles} from '../../user/user-roles.enum';

@Component({
    selector: 'app-referee-tournament-list-container',
    template: `
    <app-referee-tournament-list [tournaments]="tournaments$ | async"
                                 [loading]="loading$ | async">
    </app-referee-tournament-list>
  `,
    styles: ``,
    standalone: false
})
export class RefereeTournamentListContainerComponent implements OnInit, OnDestroy {
  tournaments$: Observable<Tournament[]>;
  loading$: Observable<boolean>;

  private subscriptions: Subscription = new Subscription();

  constructor(private tournamentConfigService: TournamentConfigService,
              private authenticationService: AuthenticationService,
              private linearProgressBarService: LinearProgressBarService) {
    this.setupProgressIndicator();
  }

  ngOnInit(): void {
    // Define the target profile ID once
    const currentUserProfileId = this.authenticationService.getCurrentUserProfileId();

    // get all tournaments but filter out those which the current user is not a Referee for
    this.tournaments$ = this.tournamentConfigService.getAll().pipe(
      map(tournaments => tournaments.filter(tournament => {
        // Safely access the personnel list
        const personnelList = tournament.configuration?.personnelList || [];
        // Check if current user exists in the list with the specific role
        return personnelList.some(personnel =>
          personnel.profileId === currentUserProfileId &&
          personnel.role === UserRoles.ROLE_REFEREES
        );
      }))
    );
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
}
