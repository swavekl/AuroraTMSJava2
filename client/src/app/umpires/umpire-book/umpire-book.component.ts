import {Component, OnDestroy} from '@angular/core';
import {Observable, of, Subscription} from 'rxjs';
import {UmpiringService} from '../service/umpiring.service';
import {AuthenticationService} from '../../user/authentication.service';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {UserRoles} from '../../user/user-roles.enum';
import {UmpiredMatchInfo} from '../model/umpired-match-info.model';

@Component({
    selector: 'app-umpire-book',
    templateUrl: './umpire-book.component.html',
    styleUrl: './umpire-book.component.scss',
    standalone: false
})
export class UmpireBookComponent implements OnDestroy{

  umpireMatchInfos$: Observable<UmpiredMatchInfo[]>;

  umpireName: string = "";

  private subscriptions: Subscription = new Subscription();

  constructor(private umpiringService: UmpiringService,
              private authenticationService: AuthenticationService,
              private linearProgressBarService: LinearProgressBarService) {
    this.setupProgressIndicator();
    this.loadUmpireMatches();
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  private setupProgressIndicator() {
    const loadingSubscription = this.umpiringService.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
  }

  private loadUmpireMatches() {
    const isUmpire = this.authenticationService.hasCurrentUserRole([UserRoles.ROLE_UMPIRES]);
    if (isUmpire) {
      this.umpireName = this.authenticationService.getCurrentUserLastName()
        + ", " + this.authenticationService.getCurrentUserFirstName();
      const currentUserProfileId = this.authenticationService.getCurrentUserProfileId();
      this.umpireMatchInfos$ = this.umpiringService.getUmpireMatches(currentUserProfileId, 0);
    } else {
      this.umpireName = "";
      this.umpireMatchInfos$ = of([])
    }
  }
}
