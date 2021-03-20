import {Component, OnInit} from '@angular/core';
import {AccountService} from '../service/account.service';
import {Observable} from 'rxjs';
import {AuthenticationService} from '../../user/authentication.service';
import {ActivatedRoute} from '@angular/router';

@Component({
  selector: 'app-account-onboard-finish',
  templateUrl: './account-onboard-finish.component.html',
  styleUrls: ['./account-onboard-finish.component.css']
})
export class AccountOnboardFinishComponent implements OnInit {

  accountActivated$: Observable<boolean>;

  private userProfileId: string;

  constructor(private accountService: AccountService,
              private authenticationService: AuthenticationService,
              private activatedRoute: ActivatedRoute) {
    this.userProfileId = this.activatedRoute.snapshot.params['userProfileId'] || '';
  }

  ngOnInit(): void {
    this.accountActivated$ = this.accountService.completeConfiguration(this.userProfileId);
  }

}
