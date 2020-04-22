import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {AuthenticationService} from '../authentication.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {

  firstName = 'Mario';
  lastName = 'Lorenc';
  email = 'swaveklorenc+mario@gmail.com';
  password = 'Mario1234';
  password2 = 'Mario1234';

  status = '';

  constructor(
    private authenticationService: AuthenticationService,
    private route: ActivatedRoute,
    private router: Router
  ) {
  }

  ngOnInit() {
    this.status = '';
  }

  register() {
    this.authenticationService.register(this.firstName, this.lastName, this.email, this.password, this.password2)
      .subscribe(data => {
        console.log ('registration successful');
            this.router.navigate(['/registrationconfirmed']);
        },
        error => {
          console.log ('error registering', error._body);
          if (error._body) {
            this.status = error._body;
          }
        });
  }
}
