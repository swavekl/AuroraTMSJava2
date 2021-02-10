import {Component, OnInit} from '@angular/core';
import {AuthenticationService} from '../../user/authentication.service';

// import { OktaAuthService } from '@okta/okta-angular';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  playerName: string;
  playerRating: string;
  membershipExpires: Date;

  constructor(private authenticationService: AuthenticationService) {
    this.playerRating = 'Unrated';
    this.membershipExpires = new Date();
  }

  ngOnInit(): void {
    this.playerName = this.authenticationService.getCurrentUserFirstName();
    this.playerRating = '' + 1793;
  }

  public isMembershipExpired() {
    const today = new Date();
    return this.membershipExpires < today;
  }


}
