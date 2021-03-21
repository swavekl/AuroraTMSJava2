import {Component, OnInit} from '@angular/core';
import {AccountService} from '../service/account.service';
import {AuthenticationService} from '../../user/authentication.service';
import {first} from 'rxjs/operators';

@Component({
  selector: 'app-account-onboard-start',
  templateUrl: './account-onboard-start.component.html',
  styleUrls: ['./account-onboard-start.component.css']
})
export class AccountOnboardStartComponent implements OnInit {

  constructor(private accountService: AccountService,
              private authenticationService: AuthenticationService) {
  }

  ngOnInit(): void {
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
