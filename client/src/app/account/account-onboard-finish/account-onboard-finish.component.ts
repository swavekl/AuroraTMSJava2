import {Component, OnInit} from '@angular/core';
import {AccountService} from '../service/account.service';
import {Observable, of} from 'rxjs';
import {AuthenticationService} from '../../user/authentication.service';
import {ActivatedRoute} from '@angular/router';

@Component({
    selector: 'app-account-onboard-finish',
    templateUrl: './account-onboard-finish.component.html',
    styleUrls: ['./account-onboard-finish.component.css'],
    standalone: false
})
export class AccountOnboardFinishComponent implements OnInit {

  accountActivated$: Observable<boolean>;


  constructor(private accountService: AccountService,
              private authenticationService: AuthenticationService,
              private activatedRoute: ActivatedRoute) {
    const userProfileId = this.activatedRoute.snapshot.params['userProfileId'] || '';
    if (userProfileId) {
      this.accountActivated$ = this.accountService.completeConfiguration(userProfileId);
    } else {
      this.accountActivated$ = of (false);
    }
  }

  ngOnInit(): void {
  }

}
