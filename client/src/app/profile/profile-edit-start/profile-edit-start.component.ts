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
              private usattPlayerRecordService: UsattPlayerRecordService) {
    this.firstName = this.authenticationService.getCurrentUserFirstName();
    this.lastName = this.authenticationService.getCurrentUserLastName();
    this.profileId = this.authenticationService.getCurrentUserProfileId();
    this.playerRecordFound = false;
  }

  ngOnInit(): void {
    console.log(`searching for player ${this.firstName} ${this.lastName}`);
    // search just once maybe we get lucky
    this.usattPlayerRecordService.searchByNames(this.firstName, this.lastName)
      .pipe(first())
      .subscribe((records: UsattPlayerRecord[]) => {
        console.log('got usatt player records', records);
        if (records != null && records.length === 1) {
          this.playerRecordFound = true;
          this.playerRecord = records[0];
        } else {
          this.playerRecordFound = false;
        }
      });
  }
}
