import {Component, OnDestroy, OnInit} from '@angular/core';
import {AuthenticationService} from '../authentication.service';
import {CrossFieldErrorMatcher} from '../cross-field-error-matcher/cross-field-error-matcher';
import {first} from 'rxjs/operators';
import {Subscription} from 'rxjs';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {PasswordCriteria} from '../password-criteria';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit, OnDestroy {

  firstName = '';
  lastName = '';
  email = '';
  password = '';
  password2 = '';

  passwordCriteria: PasswordCriteria = new PasswordCriteria();

  crossFieldErrorMatcher = new CrossFieldErrorMatcher();

  public message: string;
  public okMessage: string;
  public registrationInProgress: boolean;
  public registrationCompleted: boolean;
  private subscription: Subscription = new Subscription();

  constructor(
    private authenticationService: AuthenticationService,
    private linearProgressBarService: LinearProgressBarService) {
  }

  ngOnInit() {
    this.message = '';
    this.okMessage = '';
    this.registrationInProgress = false;
    this.registrationCompleted = false;
  }

  register() {
    this.registrationInProgress = true;
    this.linearProgressBarService.setLoading(true);

    const subscription = this.authenticationService.register(this.firstName, this.lastName, this.email, this.password, this.password2)
      .pipe(first())
      .subscribe(
        data => {
          this.message = '';
          this.okMessage = 'Email was sent to your email account.  Please follow instruction in the email to continue...';
          this.registrationInProgress = false;
          this.registrationCompleted = true;
          this.linearProgressBarService.setLoading(false);
        },
        error => {
            // console.log('error registering', error);
            const causes = error?.error?.errorCauses || '{}';
            this.message = 'Error was encountered during registration: ' + JSON.stringify(causes);
            this.okMessage = '';
          this.registrationInProgress = false;
          this.linearProgressBarService.setLoading(false);
        });
    this.subscription.add(subscription);
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }
}
