import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {AuthenticationService} from '../authentication.service';
import {CrossFieldErrorMatcher} from '../cross-field-error-matcher/cross-field-error-matcher';
import {PasswordCriteria} from '../password-criteria';

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.css']
})
export class ResetPasswordComponent implements OnInit {
  password: string;
  password2: string;
  crossFieldErrorMatcher = new CrossFieldErrorMatcher();
  resetPasswordToken: string;

  passwordCriteria: PasswordCriteria = new PasswordCriteria();

  showPassword: boolean = false;
  showPassword2: boolean = false;

  constructor(private activatedRoute: ActivatedRoute,
              private router: Router,
              private authenticationService: AuthenticationService) {
  }

  ngOnInit(): void {
    this.resetPasswordToken = this.activatedRoute.snapshot.params['resetPasswordToken'];
  }

  resetPassword() {
    this.authenticationService.resetPassword(this.resetPasswordToken, this.password)
      .subscribe(
      data => {
        // console.log ('reset password data', data);
        const succeeded: boolean = (data && data?.status === 'SUCCESS');
        this.router.navigate(['/ui/resetpasswordresult/', succeeded]);
      },
      error => {
        const url = `/ui/resetpasswordresult/false`;
        this.router.navigate([url]);
      }
    );
  }

  toggleShowPassword() {
    this.showPassword = !this.showPassword;
  }

  toggleShowPassword2() {
    this.showPassword2 = !this.showPassword2;
  }

}
