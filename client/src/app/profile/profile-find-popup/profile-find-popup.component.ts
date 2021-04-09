import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {Observable} from 'rxjs';
import {ProfileService} from '../profile.service';

/**
 * Popup for searching player profiles in OKTA
 */
@Component({
  selector: 'app-player-find-popup',
  templateUrl: './profile-find-popup.component.html',
  styleUrls: ['./profile-find-popup.component.css']
})
export class ProfileFindPopupComponent implements OnInit {
  // search criteria
  firstName: string;
  lastName: string;

  foundPlayers$: Observable<any>;

  constructor(public dialogRef: MatDialogRef<ProfileFindPopupComponent>,
              @Inject(MAT_DIALOG_DATA) public data: ProfileSearchData,
              private profileService: ProfileService) {
    this.firstName = data?.firstName;
    this.lastName = data?.lastName;
  }

  ngOnInit(): void {
  }

  onSearch() {
    const searchCriteria = [];
    if (this.firstName != null) {
      searchCriteria.push({name: 'firstName', value: this.firstName});
    }
    if (this.lastName != null) {
      searchCriteria.push({name: 'lastName', value: this.lastName});
    }
    this.foundPlayers$ = this.profileService.findProfiles(searchCriteria);
  }

  onSelection(userId: number, firstName: string, lastName: string) {
    const selectedPlayerData = {
      firstName: firstName, lastName: lastName, id: userId, rating: 1239
    };
    // todo - this is actually player profile not usatt record
    this.dialogRef.close({action: 'ok', selectedPlayerRecord: selectedPlayerData});
  }

  onCancel(): void {
    this.dialogRef.close({action: 'cancel'});
  }
}

export class ProfileSearchData {
  firstName: string;
  lastName: string;
}
