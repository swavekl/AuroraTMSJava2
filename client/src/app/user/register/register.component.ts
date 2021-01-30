import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {AuthenticationService} from '../authentication.service';
import {CrossFieldErrorMatcher} from '../cross-field-error-matcher/cross-field-error-matcher';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {

  firstName = 'Julia';
  lastName = 'Lorenc';
  email = 'swaveklorenc+julia@gmail.com';
  password = 'Julia1234';
  password2 = 'Julia1234';

  crossFieldErrorMatcher = new CrossFieldErrorMatcher();

  public message: string;
  public done: boolean;

  constructor(
    private authenticationService: AuthenticationService,
    private router: Router
  ) {
  }

  ngOnInit() {
    this.message = '';
    this.done = false;
  }

  register() {
    this.done = false;
    this.authenticationService.register(this.firstName, this.lastName, this.email, this.password, this.password2)
      .subscribe(
        data => {
          this.message = 'Email was sent to your email account.  Please follow instruction in the email to continue...';
          // this.router.navigate(['/registrationconfirmed']);
          this.done = true;
        },
        error => {
            console.log('error registering', error);
            const causes = error?.error?.errorCauses || '{}';
            this.message = 'Error was encountered during registration: ' + JSON.stringify(causes);
          this.done = true;
        });
  }
}
