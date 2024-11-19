import {Component, OnDestroy, OnInit} from '@angular/core';
import {AuthenticationService} from '../authentication.service';
import {CrossFieldErrorMatcher} from '../cross-field-error-matcher/cross-field-error-matcher';
import {first} from 'rxjs/operators';
import {Subscription} from 'rxjs';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {PasswordCriteria} from '../password-criteria';
import {Router} from '@angular/router';
import {TitleCasePipe} from '@angular/common';

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

  showPassword: boolean = false;
  showPassword2: boolean = false;

  private registerByTD: boolean = false;
  private tournamentId: string = null;


  constructor(private authenticationService: AuthenticationService,
              private linearProgressBarService: LinearProgressBarService,
              private router: Router) {
  }

  ngOnInit() {
    this.message = '';
    this.okMessage = '';
    this.registrationInProgress = false;
    this.registrationCompleted = false;
    this.registerByTD = history?.state?.registerByTD === true;
    this.tournamentId = history?.state?.tournamentId;
  }

  register() {
    // make the names properly cased i.e. first letter upper case, the rest lower case
    // iPhone users type them all in uppercase
    const titleCasePipe = new TitleCasePipe();
    let firstName = titleCasePipe.transform(this.firstName);
    let lastName = titleCasePipe.transform(this.lastName);
    firstName = firstName.trim();
    lastName = lastName.trim();
    // console.log(`'${firstName}' '${lastName}'`);
    if (firstName.length > 0 && lastName.length > 0) {
      this.registrationInProgress = true;
      this.linearProgressBarService.setLoading(true);

      const subscription = this.authenticationService.register(firstName, lastName, this.email, this.password, this.password2, this.registerByTD)
        .pipe(first())
        .subscribe(
          {
            next: (profileId: string) => {
              this.message = '';
              this.registrationInProgress = false;
              this.registrationCompleted = true;
              this.linearProgressBarService.setLoading(false);
              if (this.registerByTD) {
                const extras = {
                  state: {
                    initializingProfile: true,
                    forwardUrl: `/ui/tournaments/playerlistbig/${this.tournamentId}`,
                    returnUrl: `/ui/entries/entryadd/${this.tournamentId}/${profileId}`
                  }
                };
                this.router.navigateByUrl(`/ui/userprofile/edit/${profileId}`, extras);
              } else {
                this.okMessage = `Account activation email was sent to ${this.email ? this.email : 'your'}.</p><p>Please allow up to 1 minute for email to arrive and if you still cannot find it, check your SPAM or JUNK</span> folder.</p><p>Look for email from Okta with title 'Welcome to TTAurora'.`;
              }
            },
            error: error => {
              // console.log('error registering', error);
              this.message = error.error;
              this.okMessage = '';
              this.registrationInProgress = false;
              this.linearProgressBarService.setLoading(false);
            }
          });
      this.subscription.add(subscription);
    } else {
      this.message = 'Please provide non-empty first and last name';
      this.okMessage = '';
    }
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  toggleShowPassword() {
    this.showPassword = !this.showPassword;
  }

  toggleShowPassword2() {
    this.showPassword2 = !this.showPassword2;
  }

}
