import { Component, OnInit } from '@angular/core';
import {Observable, of} from 'rxjs';
import {AccountService} from '../service/account.service';
import {AuthenticationService} from '../../user/authentication.service';

@Component({
  selector: 'app-account-landing',
  templateUrl: './account-landing.component.html',
  styleUrls: ['./account-landing.component.css']
})
export class AccountLandingComponent implements OnInit {

  accountConfigured$: Observable<boolean>;

  constructor(private accountService: AccountService,
              private authenticationService: AuthenticationService) {
  }

  ngOnInit(): void {
    const userProfileId = this.authenticationService.getCurrentUserProfileId();
    this.accountConfigured$ = this.accountService.isAccountConfigured(userProfileId);
  }


}
