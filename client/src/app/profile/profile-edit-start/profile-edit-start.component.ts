import {Component, OnInit} from '@angular/core';
import {AuthenticationService} from '../../user/authentication.service';
import {UsattPlayerRecordService} from '../service/usatt-player-record.service';
import {UsattPlayerRecord} from '../model/usatt-player-record.model';
import {first} from 'rxjs/operators';
import {Router} from '@angular/router';

@Component({
  selector: 'app-profile-edit-start',
  templateUrl: './profile-edit-start.component.html',
  styleUrls: ['./profile-edit-start.component.css']
})
export class ProfileEditStartComponent implements OnInit {
  firstName: string;
  lastName: string;
  playerRecordFound: boolean;
  playerRecord: UsattPlayerRecord;
  profileId: string;

  constructor(private authenticationService: AuthenticationService,
              private usattPlayerRecordService: UsattPlayerRecordService,
              private router: Router) {
    this.firstName = this.authenticationService.getCurrentUserFirstName();
    this.lastName = this.authenticationService.getCurrentUserLastName();
    this.profileId = this.authenticationService.getCurrentUserProfileId();
    this.playerRecordFound = false;
  }

  ngOnInit(): void {
    // console.log(`searching for player ${this.firstName} ${this.lastName}`);
    // search just once maybe we get lucky
    this.usattPlayerRecordService.getByNames(this.firstName, this.lastName)
      .pipe(first())
      .subscribe((record: UsattPlayerRecord) => {
        // console.log('got usatt player records', record);
        if (record != null) {
          this.playerRecordFound = true;
          this.playerRecord = record;
        } else {
          this.playerRecordFound = false;
        }
      });
  }

  onSelectedPlayer(playerRecord: UsattPlayerRecord) {
    // console.log ('using this player record for profile init', playerRecord);
    const state = {initializingProfile: true, playerRecord: playerRecord};
    const url = `/userprofile/${this.profileId}`;
    this.router.navigate([url], {state: state});
  }
}
