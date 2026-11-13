import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {AuthenticationService} from '../authentication.service';
import {CrossFieldErrorMatcher} from '../cross-field-error-matcher/cross-field-error-matcher';
import {finalize, first} from 'rxjs/operators';
import {Subscription} from 'rxjs';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {PasswordCriteria} from '../password-criteria';
import {Router} from '@angular/router';
import {TitleCasePipe} from '@angular/common';

@Component({
    selector: 'app-register',
    templateUrl: './register.component.html',
    styleUrls: ['./register.component.scss'],
    standalone: false
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
  public resendInProgress: boolean;
  public resendLimitReached: boolean;
  public verificationMessage: string;
  public resendMessage: string;
  private resendAttempts: number;
  private subscription: Subscription = new Subscription();

  @ViewChild('f') registerForm: NgForm;

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
    this.resendInProgress = false;
    this.resendLimitReached = false;
    this.resendAttempts = 0;
    this.verificationMessage = '';
    this.resendMessage = '';
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
            next: (result: any) => {
              const profileId = result?.profileId;
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
                this.resendAttempts = 0;
                this.resendLimitReached = false;
                this.resendMessage = '';
                this.verificationMessage = result?.verificationEmailSent
                  ? `We sent a verification email to ${this.email}. Please allow up to a minute for it to arrive.`
                  : `We could not send the verification email right now. You can try again using the button below.`;
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

  resendVerificationEmail() {
    if (this.resendInProgress || !this.email) {
      return;
    }
    this.resendInProgress = true;
    this.linearProgressBarService.setLoading(true);
    this.resendMessage = '';
    const subscription = this.authenticationService.resendVerificationEmail(this.email)
      .pipe(
        first(),
        finalize(() => {
          this.resendInProgress = false;
          this.linearProgressBarService.setLoading(false);
        })
      )
      .subscribe({
        next: (response: any) => {
          this.resendMessage = response?.message || 'Another email was sent. Please check again.';
          this.recordResendAttempt();
        },
        error: () => {
          this.resendMessage = 'We could not process that request. Please wait a moment and try again.';
          this.recordResendAttempt();
        }
      });
    this.subscription.add(subscription);
  }

  startOver() {
    this.email = '';
    this.password = '';
    this.password2 = '';
    this.message = '';
    this.okMessage = '';
    this.verificationMessage = '';
    this.resendMessage = '';
    this.registrationCompleted = false;
    this.resendInProgress = false;
    this.resendAttempts = 0;
    this.resendLimitReached = false;
    this.registerForm?.resetForm({
      firstName: this.firstName,
      lastName: this.lastName,
      email: this.email,
      passwords: {password: this.password, password2: this.password2}
    });
    this.router.navigateByUrl('/ui/login/register');
  }

  private recordResendAttempt() {
    this.resendAttempts++;
    if (this.resendAttempts >= 2) {
      this.resendLimitReached = true;
      this.resendMessage = '';
    }
  }

  toggleShowPassword() {
    this.showPassword = !this.showPassword;
  }

  toggleShowPassword2() {
    this.showPassword2 = !this.showPassword2;
  }

}
