import {Component, OnDestroy, OnInit} from '@angular/core';
import {BehaviorSubject, Observable, Subscription} from 'rxjs';
import {AccountService, AccountStatus} from '../service/account.service';
import {AuthenticationService} from '../../user/authentication.service';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {first} from 'rxjs/operators';

@Component({
  selector: 'app-account-landing',
  templateUrl: './account-landing.component.html',
  styleUrls: ['./account-landing.component.css']
})
export class AccountLandingComponent implements OnInit, OnDestroy {

  accountStatus$: Observable<AccountStatus>;

  loading$: BehaviorSubject<boolean>;

  private subscriptions: Subscription = new Subscription();

  constructor(private accountService: AccountService,
              private authenticationService: AuthenticationService,
              private linearProgressBarService: LinearProgressBarService) {

    this.loading$ = new BehaviorSubject<boolean>(true);
    const subscription = this.accountService.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
      this.loading$.next(loading);
    });
    this.subscriptions.add(subscription);

    const userProfileId = this.authenticationService.getCurrentUserProfileId();
    this.accountStatus$ = this.accountService.getAccountStatus(userProfileId);
  }

  ngOnInit(): void {
  }

  /**
   * Resumes account configuration
   */
  onResumeAccount() {
    console.log('resuming account configuration');
    const userProfileId = this.authenticationService.getCurrentUserProfileId();
    this.accountService.resumeAccountConfiguration(userProfileId)
      .pipe(first())
      .subscribe((accountLinkUrl: string) => {
        console.log('got resume account link url - navigating to ' + accountLinkUrl);
        window.location.href = accountLinkUrl;
      });
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }
}
