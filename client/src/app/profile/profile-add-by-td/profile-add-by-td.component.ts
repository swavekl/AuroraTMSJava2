import {Component, EventEmitter, OnDestroy, OnInit, Output} from '@angular/core';
import {RecordSearchData} from '../usatt-record-search-popup/usatt-record-search-popup.component';
import {UsattRecordSearchCallbackData, UsattRecordSearchPopupService} from '../service/usatt-record-search-popup.service';
import {UsattPlayerRecord} from '../model/usatt-player-record.model';
import {ActivatedRoute, Router} from '@angular/router';
import {ProfileFindPopupComponent, ProfileSearchData} from '../profile-find-popup/profile-find-popup.component';
import {MatDialog} from '@angular/material/dialog';
import {Subscription} from 'rxjs';
import {Profile} from '../profile';

@Component({
  selector: 'app-profile-add-by-td',
  templateUrl: './profile-add-by-td.component.html',
  styleUrls: ['./profile-add-by-td.component.scss']
})
export class ProfileAddByTDComponent implements OnInit, OnDestroy {

  @Output()
  createProfile: EventEmitter<Profile> = new EventEmitter();

  @Output()
  createNewProfile: EventEmitter<any> = new EventEmitter();

  @Output()
  useProfile: EventEmitter<string> = new EventEmitter();

  @Output()
  cancel: EventEmitter<any> = new EventEmitter();

  private subscriptions: Subscription = new Subscription();

  firstName: string;
  lastName: string;
  email: string;
  playerProfileFound: boolean = false;
  playerRecordFound: boolean = false;
  playerRecord: UsattPlayerRecord;
  private tournamentId: string;
  private profileId: string;

  constructor(private playerFindPopupService: UsattRecordSearchPopupService,
              private dialog: MatDialog,
              private router: Router,
              private activatedRoute: ActivatedRoute) {
    this.tournamentId = this.activatedRoute.snapshot.params['tournamentId'];
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  onFindPlayerById() {
    this.findPlayer(true);
  }

  onFindPlayerByName() {
    this.findPlayer(false);
  }

  private findPlayer(searchById: boolean) {
    const data: RecordSearchData = {
      firstName: null,
      lastName: null,
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
    me.email = null;
    me.playerRecordFound = true;
    me.playerProfileFound = false;
  }

  findPlayerProfile () {
    const profileSearchData: ProfileSearchData = {
      firstName: null,
      lastName: null
    };
    const config = {
      width: '400px', height: '550px', data: profileSearchData
    };

    const dialogRef = this.dialog.open(ProfileFindPopupComponent, config);
    const subscription = dialogRef.afterClosed().subscribe(result => {
      if (result?.action === 'ok') {
        this.playerProfileFound = true;
        this.playerRecordFound = false;
        const playerData = result.selectedPlayerRecord;
        // console.log('selected player data ', playerData);
        this.profileId = playerData.id;
        this.firstName = playerData.firstName;
        this.lastName = playerData.lastName;
        this.email = playerData.email;
        this.playerRecord = {
          membershipId: playerData.membershipId,
          membershipExpirationDate: playerData.membershipExpirationDate,
          firstName: playerData.firstName,
          lastName: playerData.lastName,
          dateOfBirth: playerData.dateOfBirth,
          gender: playerData.gender,
          city: null,
          state: playerData.state,
          zip: playerData.zipCode,
          homeClub: null,
          tournamentRating: playerData.rating,
          lastTournamentPlayedDate: null,
          leagueRating: null,
          lastLeaguePlayedDate: null
        };
        // console.log('playerRecord from profile ', this.playerRecord);

      }
    });
    this.subscriptions.add(subscription);
  }

  onCreateProfile () {
    const usattPlayerRecord: UsattPlayerRecord = this.playerRecord;
    const profile: Profile = new Profile();
    profile.email = this.email;
    profile.login = this.email;
    profile.firstName = usattPlayerRecord.firstName;
    profile.lastName = usattPlayerRecord.lastName;
    profile.membershipId = usattPlayerRecord.membershipId;
    profile.state = usattPlayerRecord.state;
    profile.zipCode = usattPlayerRecord.zip;
    profile.gender = (usattPlayerRecord.gender === 'F') ? 'Female' : 'Male';
    profile.dateOfBirth = usattPlayerRecord.dateOfBirth;
    profile.countryCode = 'US';

    this.createProfile.emit(profile);
  }

  onCreateNewProfile() {
    this.createNewProfile.emit(null);
  }

  onUseProfile () {
    this.useProfile.emit(this.profileId);
  }

  isEmailRequired() {
    return this.playerRecordFound && !this.playerProfileFound;
  }

  onCancel() {
    this.cancel.emit(null);
  }

  isCreatingProfile() {
    return !this.playerProfileFound && this.playerRecordFound;
  }

  isMakingNewProfile() {
    return !this.playerProfileFound && !this.playerRecordFound;
  }
}
