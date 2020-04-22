import {Component, OnInit} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ActivatedRoute, Router} from '@angular/router';
import {AuthenticationService} from '../../user/authentication.service';

@Component({
  selector: 'app-user-welcome',
  templateUrl: './user-welcome.component.html',
  styleUrls: ['./user-welcome.component.css']
})
export class UserWelcomeComponent implements OnInit {

  firstName: string;
  lastName: string;
  username: string;
  activationResult: boolean;

  constructor(private httpClient: HttpClient,
              private activatedRoute: ActivatedRoute,
              private router: Router,
              private authenticationService: AuthenticationService) {
  }

  ngOnInit() {
    this.username = this.activatedRoute.snapshot.queryParamMap.get('email');
    this.username = this.username.replace(' ', '+');
    const activationToken = this.activatedRoute.snapshot.queryParamMap.get('token');
    this.authenticationService.validateEmail(this.username, activationToken)
      .subscribe(data => {
        console.log('activation successful', data);
        if (data != null) {
          this.activationResult = true;
          this.firstName = 'Mario';
          this.lastName = 'Lorenc';
          // this.router.navigate(['/userwelcome']);
        }
      },
      error => {
        this.activationResult = false;
        if (error._body) {
          console.log('error activating', error._body);
        }
      });

  }
}
