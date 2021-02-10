import {Component, OnInit} from '@angular/core';
import {AuthenticationService} from '../authentication.service';
import {ActivatedRoute, Router} from '@angular/router';
import {Store} from '@ngrx/store';
import {ResetStore} from '../../store/reset-store';
import {first} from 'rxjs/operators';
import {Subject} from 'rxjs';

@Component({
  selector: 'app-sign-in',
  templateUrl: './sign-in.component.html',
  styleUrls: ['./sign-in.component.css']
})
export class SignInComponent implements OnInit {

  // username/email
  email: string;
  // password
  password: string;

  // error status
  status: string;
  returnUrl: string;

  // progress indicator
  loginInProgress$: Subject<boolean> = new Subject<boolean>();

  constructor(private authenticationService: AuthenticationService,
              private activatedRoute: ActivatedRoute,
              private router: Router,
              private store: Store) {
    this.returnUrl = activatedRoute.snapshot.queryParamMap.get('returnUrl');
    this.returnUrl = (this.returnUrl != null) ? this.returnUrl : '/home';
  }

  ngOnInit() {
  }

  login() {
    this.loginInProgress$.next(true);
    this.status = '';
    this.store.dispatch(new ResetStore());
    this.authenticationService.login(this.email, this.password)
      .pipe(first())
      .subscribe(data => {
        // console.log ('got login data', data);
          if (data === true) {
            this.status = 'Success';
            this.router.navigate([this.returnUrl]);
          } else {
            this.status = 'Invalid username and/or password.';
          }
        },
        error => {
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
}
