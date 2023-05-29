import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {Observable} from 'rxjs';
import {ProfileService} from '../profile.service';
import {Profile} from '../profile';

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
  loading$: Observable<boolean>;

  title: string;

  constructor(public dialogRef: MatDialogRef<ProfileFindPopupComponent>,
              @Inject(MAT_DIALOG_DATA) public data: ProfileSearchData,
              private profileService: ProfileService) {
    this.firstName = data?.firstName;
    this.lastName = data?.lastName;
    this.title = (data?.dialogTitle != null) ? data?.dialogTitle : 'Find Player';
    this.loading$ = this.profileService.loading$;
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

  onSelection(profile: Profile) {
    const selectedPlayerData = {
      firstName: profile.firstName,
      lastName: profile.lastName,
      id: profile.userId,
      rating: profile.tournamentRating,
      state: profile.state,
      zipCode: profile.zipCode,
      gender: profile.gender,
      membershipExpirationDate: profile.membershipExpirationDate,
      membershipId: profile.membershipId
    };
    this.dialogRef.close({action: 'ok', selectedPlayerRecord: selectedPlayerData});
  }

  onCancel(): void {
    this.dialogRef.close({action: 'cancel'});
  }
}

export class ProfileSearchData {
  firstName: string;
  lastName: string;
  dialogTitle?: string;
}
