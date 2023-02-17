import {Component, OnInit} from '@angular/core';
import {AuthenticationService} from '../../user/authentication.service';
import {UsattPlayerRecordService} from '../service/usatt-player-record.service';
import {UsattPlayerRecord} from '../model/usatt-player-record.model';
import {first} from 'rxjs/operators';
import {ActivatedRoute, Router} from '@angular/router';
import {UsattRecordSearchCallbackData, UsattRecordSearchPopupService} from '../service/usatt-record-search-popup.service';
import {RecordSearchData} from '../usatt-record-search-popup/usatt-record-search-popup.component';

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
              private playerFindPopupService: UsattRecordSearchPopupService,
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

  // onSelectedPlayer(playerRecord: UsattPlayerRecord) {
  //   // console.log ('using this player record for profile init', playerRecord);
  //   const state = {initializingProfile: true, playerRecord: playerRecord};
  //   const url = `/userprofile/${this.profileId}`;
  //   this.router.navigate([url], {state: state});
  // }

  onProfileEditStart(initializingProfile: boolean, playerRecord: UsattPlayerRecord) {
    const state = {initializingProfile: initializingProfile, playerRecord: playerRecord};
    const url = `/userprofile/edit/${this.profileId}`;
    this.router.navigate([url], {state: state});
  }

  onFindPlayerById() {
    this.findPlayer(true);
  }

  onFindPlayerByName() {
    this.findPlayer(false);
  }

  private findPlayer(searchById: boolean) {
    const data: RecordSearchData = {
      firstName: this.firstName,
      lastName: this.lastName,
      searchingByMembershipId: searchById
    };
    const callbackParams: UsattRecordSearchCallbackData = {
      successCallbackFn: this.onFindPlayerOkCallback,
      cancelCallbackFn: null,
      callbackScope: this
    };
    this.playerFindPopupService.showPopup(data, callbackParams);
  }

  onFindPlayerOkCallback(scope: any, selectedPlayerRecord: UsattPlayerRecord) {
    const me = scope;
    me.playerRecord = selectedPlayerRecord;
    me.firstName = selectedPlayerRecord.firstName;
    me.lastName = selectedPlayerRecord.lastName;
    me.playerRecordFound = true;
  }
}
