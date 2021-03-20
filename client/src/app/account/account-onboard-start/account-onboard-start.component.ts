import { Component, OnInit } from '@angular/core';
import {Observable, of} from 'rxjs';
import {AccountService} from '../service/account.service';
import {AuthenticationService} from '../../user/authentication.service';
import {first} from 'rxjs/operators';
import {Router} from '@angular/router';

@Component({
  selector: 'app-account-onboard-start',
  templateUrl: './account-onboard-start.component.html',
  styleUrls: ['./account-onboard-start.component.css']
})
export class AccountOnboardStartComponent implements OnInit {

  accountConfigured$: Observable<boolean>;

  constructor(private accountService: AccountService,
              private authenticationService: AuthenticationService,
              private router: Router) {
    this.accountConfigured$ = of(false);
  }

  /**
   *
   */
  ngOnInit(): void {
    const userProfileId = this.authenticationService.getCurrentUserProfileId();
    this.accountConfigured$ = this.accountService.isAccountConfigured(userProfileId);
  }

  /**
   *
   */
  onCreateAccount() {
    const userProfileId = this.authenticationService.getCurrentUserProfileId();
    this.accountService.createAccount(userProfileId)
      .pipe(first())
      .subscribe((accountLinkUrl: string) => {
        console.log('got account link url - navigating to ' + accountLinkUrl);
        window.location.href = accountLinkUrl;
      });
  }
}
