import {Component, OnDestroy, OnInit} from '@angular/core';
import {AuthenticationService} from '../authentication.service';
import {ActivatedRoute, Router} from '@angular/router';
import {Store} from '@ngrx/store';
import {ResetStore} from '../../store/reset-store';
import {first} from 'rxjs/operators';
import {Subject, Subscription} from 'rxjs';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';

@Component({
  selector: 'app-sign-in',
  templateUrl: './sign-in.component.html',
  styleUrls: ['./sign-in.component.scss']
})
export class SignInComponent implements OnInit, OnDestroy {

  // username/email
  email: string;
  // password
  password: string;

  // error status
  status: string;
  // inidicates if the status message is an error and should be shown in red or not
  isSuccess: boolean;

  returnUrl: string;

  showPassword: boolean = false;

  // progress indicator
  loginInProgress$: Subject<boolean> = new Subject<boolean>();

  private subscriptions: Subscription = new Subscription();

  constructor(private authenticationService: AuthenticationService,
              private activatedRoute: ActivatedRoute,
              private router: Router,
              private store: Store,
              private linearProgressBarService: LinearProgressBarService) {
    this.returnUrl = activatedRoute.snapshot.queryParamMap.get('returnUrl');
    this.returnUrl = (this.returnUrl != null) ? this.returnUrl : '/ui/home';
    this.isSuccess = false;
  }

  ngOnInit() {
    // subscription for indicating progress on global toolbar
    const subscription = this.loginInProgress$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(subscription);
  }

  login() {
    // console.log ('logging in...');
    this.loginInProgress$.next(true);
    this.status = '';
    this.store.dispatch(new ResetStore());
    this.authenticationService.login(this.email, this.password)
      .pipe(first())
      .subscribe((loginSuccessful) => {
        // console.log ('login completed with result', loginSuccessful);
        // hide progress right away
          if (loginSuccessful === true) {
            this.status = 'Success';
            this.isSuccess = true;
            this.router.navigate([this.returnUrl]);
          } else {
            this.status = 'Invalid username and/or password.';
          }
        },
        error => {
          // hide progress right away
          console.log ('error logging in', error._body);
          if (error._body) {
            this.status = error._body;
          } else if (error?.error?.error != null) {
            // this.status = error.error.error;
            this.status = 'Invalid username and/or password.';
          }
        },
        () => {
          this.loginInProgress$.next(false);
        });
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  toggleShowPassword() {
    this.showPassword = !this.showPassword;
  }
}
