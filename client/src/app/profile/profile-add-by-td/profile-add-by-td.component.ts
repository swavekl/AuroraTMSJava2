import {Component, EventEmitter, OnDestroy, OnInit, Output} from '@angular/core';
import {RecordSearchData} from '../usatt-record-search-popup/usatt-record-search-popup.component';
import {UsattRecordSearchCallbackData, UsattRecordSearchPopupService} from '../service/usatt-record-search-popup.service';
import {UsattPlayerRecord} from '../model/usatt-player-record.model';
import {ActivatedRoute, Router} from '@angular/router';
import {ProfileFindPopupComponent, ProfileSearchData} from '../profile-find-popup/profile-find-popup.component';
import {MatDialog} from '@angular/material/dialog';
import {Subscription} from 'rxjs';

@Component({
  selector: 'app-profile-add-by-td',
  templateUrl: './profile-add-by-td.component.html',
  styleUrls: ['./profile-add-by-td.component.scss']
})
export class ProfileAddByTDComponent implements OnInit, OnDestroy {

  @Output()
  createProfile: EventEmitter<UsattPlayerRecord> = new EventEmitter();

  @Output()
  useProfile: EventEmitter<string> = new EventEmitter();

  private subscriptions: Subscription = new Subscription();

  firstName: string;
  lastName: string;
  playerProfileFound: boolean;
  playerRecordFound: boolean;
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
    me.playerRecordFound = true;
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
        console.log('profile ', result.selectedPlayerRecord);
        this.profileId = result.selectedPlayerRecord.id;
        this.firstName = result.selectedPlayerRecord.firstName;
        this.lastName = result.selectedPlayerRecord.lastName;
        this.playerRecord = {
          membershipId: null,
          membershipExpirationDate: null,
          firstName: result.selectedPlayerRecord.firstName,
          lastName: result.selectedPlayerRecord.lastName,
          dateOfBirth: null,
          gender: null,
          city: null,
          state: null,
          zip: null,
          homeClub: null,
          tournamentRating: result.selectedPlayerRecord.rating,
          lastTournamentPlayedDate: null,
          leagueRating: null,
          lastLeaguePlayedDate: null
        };
        this.playerRecordFound = true;

      }
    });
    this.subscriptions.add(subscription);
  }

  onCreateProfile () {
    this.createProfile.emit(this.playerRecord);
  }

  onUseProfile () {
    this.useProfile.emit(this.profileId);
  }
}
