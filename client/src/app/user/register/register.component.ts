import {Component, OnInit} from '@angular/core';
import {AuthenticationService} from '../authentication.service';
import {CrossFieldErrorMatcher} from '../cross-field-error-matcher/cross-field-error-matcher';
import {first} from 'rxjs/operators';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit {

  firstName = 'Julia';
  lastName = 'Lorenc';
  email = 'swaveklorenc+julia@gmail.com';
  password = 'Julia1234';
  password2 = 'Julia1234';

  crossFieldErrorMatcher = new CrossFieldErrorMatcher();

  public message: string;
  public registrationInProgress: boolean;

  constructor(
    private authenticationService: AuthenticationService) {
  }

  ngOnInit() {
    this.message = '';
    this.registrationInProgress = false;
  }

  register() {
    this.registrationInProgress = true;
    this.authenticationService.register(this.firstName, this.lastName, this.email, this.password, this.password2)
      .pipe(first())
      .subscribe(
        data => {
          this.message = 'Email was sent to your email account.  Please follow instruction in the email to continue...';
          this.registrationInProgress = false;
        },
        error => {
            // console.log('error registering', error);
            const causes = error?.error?.errorCauses || '{}';
            this.message = 'Error was encountered during registration: ' + JSON.stringify(causes);
          this.registrationInProgress = false;
        });
  }
}
