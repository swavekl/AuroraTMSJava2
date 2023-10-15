import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChange,
  SimpleChanges,
  ViewChild
} from '@angular/core';
import {Profile} from '../profile';
import {StatesList} from '../../shared/states/states-list';
import {CountriesList} from '../../shared/countries-list';
import {DateUtils} from '../../shared/date-utils';
import {UsattRecordSearchCallbackData, UsattRecordSearchPopupService} from '../service/usatt-record-search-popup.service';
import {RecordSearchData} from '../usatt-record-search-popup/usatt-record-search-popup.component';
import {UsattPlayerRecord} from '../model/usatt-player-record.model';
import {Observable, Subscription} from 'rxjs';
import {Club} from '../../club/club/model/club.model';
import {ClubService} from '../../club/club/service/club.service';
import {UntypedFormControl} from '@angular/forms';
import {debounceTime, distinctUntilChanged, filter, first, skip, switchMap} from 'rxjs/operators';
import {MatAutocompleteSelectedEvent} from '@angular/material/autocomplete';
import {ClubEditCallbackData, ClubEditPopupService} from '../../club/club/service/club-edit-popup.service';
import {ClubSearchCallbackData, ClubSearchData, ClubSearchPopupService} from '../../club/club/service/club-search-popup.service';

@Component({
  selector: 'app-profile-edit',
  templateUrl: './profile-edit.component.html',
  styleUrls: ['./profile-edit.component.scss']
})
export class ProfileEditComponent implements OnInit, OnChanges, AfterViewInit, OnDestroy {

  // this is what we edit
  @Input() profile: Profile;

  // flag indicating if user will be allowed to change this player's membership id
  @Input()
  canChangeMembershipId: boolean;

  // save and cancel
  @Output() saved = new EventEmitter();
  @Output() canceled = new EventEmitter();

  // list of US states
  statesList: any [];

  countries: any [];

  maxDateOfBirth: Date;

  private readonly USA_ZIP_CODE_PATTERN = /^\d{5}(?:[-\s]\d{4})?$/;
  private readonly CAN_ZIP_CODE_PATTERN = /^[ABCEGHJ-NPRSTVXY][0-9][ABCEGHJ-NPRSTV-Z] [0-9][ABCEGHJ-NPRSTV-Z][0-9]$/;
  public zipCodePattern;
  public zipCodeLength: number;

  private subscriptions: Subscription = new Subscription();
  public filteredClubs: Club [] = [];

  // control showing home club name for autocompletion loading of clubs
  @ViewChild('homeClubName')
  private homeClubNameControl: UntypedFormControl;

  constructor(private usattRecordSearchPopupService: UsattRecordSearchPopupService,
              private clubService: ClubService,
              private clubEditPopupService: ClubEditPopupService,
              private cdr: ChangeDetectorRef,
              private clubSearchService: ClubSearchPopupService) {
    this.profile = new Profile();
    this.maxDateOfBirth = new Date();
    this.countries = CountriesList.getList();
    this.filteredClubs = [];
    this.setZipCodeOptions ('US');
  }

  ngOnInit() {
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  ngAfterViewInit(): void {
    this.initClubFilter();
  }

  onSave(formValues: any, valid: boolean) {
    if (valid) {
      const profile: Profile = new Profile();
      profile.fromFormValues(formValues);
      this.saved.emit(profile);
    }
  }

  onCancel() {
    this.canceled.emit('cancelled');
  }

  ngOnChanges(changes: SimpleChanges): void {
    // fix date of birth if this is the first time user
    // is editing profile right after registration
    const profileChanges: SimpleChange = changes.profile;
    if (profileChanges != null && !profileChanges.firstChange) {
      const strDateOfBirth = profileChanges.currentValue?.dateOfBirth;
      if (strDateOfBirth != null) {
        const dateOfBirth: Date = new DateUtils().convertFromString(strDateOfBirth);
        const today = new Date();
        if (today.getFullYear() === dateOfBirth.getFullYear()) {
          this.profile.dateOfBirth = null;
        }
      }
      this.setZipCodeOptions(this.profile?.countryCode);
      if (this.profile?.homeClubId !== null && this.profile?.homeClubName != null) {
        // make a fake result of query to be able to validate without
        // going to the server for just one club name
        const currentClub: Club = {
          id: this.profile.homeClubId,
          clubName: this.profile.homeClubName,
          alternateClubNames: null,
          streetAddress: null,
          city: null,
          state: null,
          zipCode: null,
          countryCode: null
        };
        this.filteredClubs = [currentClub];
      }
    }
  }

  onCountryChange(countryCode: any) {
    this.setZipCodeOptions(countryCode);
  }

  private setZipCodeOptions(countryCode: string) {
    this.statesList = StatesList.getCountryStatesList(countryCode);
    switch (countryCode) {
      case 'CAN':
      case 'CA':
        this.zipCodePattern = this.CAN_ZIP_CODE_PATTERN;
        this.zipCodeLength = 7;
        break;
      case 'US':
      default:
        this.zipCodePattern = this.USA_ZIP_CODE_PATTERN;
        this.zipCodeLength = 10;
        break;
    }
  }

  /**
   * Allow tournament directors or admins to change membership id for this player
   */
  onChangeMembershipId() {
    const data: RecordSearchData = {
      searchingByMembershipId: true,
      firstName: '',
      lastName: ''
    };
    const callbackParams: UsattRecordSearchCallbackData = {
      successCallbackFn: this.onFindPlayerOkCallback,
      cancelCallbackFn: null,
      callbackScope: this
    };
    this.usattRecordSearchPopupService.showPopup(data, callbackParams);
  }

  /**
   * Callback for when the different member is selected
   * @param scope this object
   * @param selectedPlayerData selected player data
   */
  onFindPlayerOkCallback(scope: any, selectedPlayerData: UsattPlayerRecord) {
    // for some reason updating doesn't work when I use scope directly
    scope.onFindPlayerOkCallbackInScope(selectedPlayerData);
  }

  onFindPlayerOkCallbackInScope(selectedPlayerData: UsattPlayerRecord) {
    const updatedProfile: Profile = new Profile();
    updatedProfile.clone(this.profile);
    updatedProfile.membershipId = selectedPlayerData.membershipId;
    updatedProfile.membershipExpirationDate = selectedPlayerData.membershipExpirationDate;
    updatedProfile.tournamentRating = selectedPlayerData.tournamentRating;
    this.profile = updatedProfile;
    this.cdr.markForCheck();
  }

  initClubFilter() {
    if (this.homeClubNameControl) {
      // whenever the home club name changes reload the list of clubs matching this string
      // for auto completion
      const subscription = this.homeClubNameControl.valueChanges
        .pipe(
          distinctUntilChanged(),
          debounceTime(250),
          skip(1), // skip form initialization phase - to save one trip to the server
          filter(homeClubName => {
            // don't query until you have a few characters
            return homeClubName && homeClubName.length >= 3;
          }),
          switchMap((homeClubName): Observable<Club []> => {
            const params = `?nameContains=${homeClubName}&sort=clubName,ASC`;
            return this.clubService.getWithQuery(params);
          })
        ).subscribe((clubs: Club []) => {
          this.filteredClubs = clubs;
          // refresh the drop down contents and show it
          this.cdr.markForCheck();
        });
      this.subscriptions.add(subscription);
    }
  }

  /**
   * Called when user choses from auto completion options
   * @param $event
   */
  onClubSuggestionSelected($event: MatAutocompleteSelectedEvent) {
    const clubName: string = $event.option.value;
    let homeClubId = null;
    for (const club of this.filteredClubs) {
      if (club.clubName === clubName) {
        homeClubId = club.id;
        break;
      }
    }
    this.updateClubInfoAndRefresh(clubName, homeClubId);
  }

  clearClubName() {
    this.filteredClubs = [];
    this.updateClubInfoAndRefresh(null, null);
  }

  private updateClubInfoAndRefresh(clubName: string, clubId: number) {
    const updatedProfile: Profile = new Profile();
    updatedProfile.clone(this.profile);
    updatedProfile.homeClubId = clubId;
    updatedProfile.homeClubName = clubName;
    this.profile = updatedProfile;
    this.filteredClubs = [];
    this.cdr.markForCheck();
  }

  onAddClub() {
    const callbackParams: ClubEditCallbackData = {
      successCallbackFn: this.onAddClubOKCallback,
      cancelCallbackFn: null,
      callbackScope: this
    };
    const newClub: Club = new Club();
    this.clubEditPopupService.showPopup(newClub, callbackParams);
  }

  onFindClub() {
    const callbackParams: ClubSearchCallbackData = {
      successCallbackFn: this.onAddClubOKCallback,
      cancelCallbackFn: null,
      callbackScope: this
    };
    const clubSearchData: ClubSearchData = {
      state: this.profile.state,
      countryCode: this.profile.countryCode
    }
    this.clubSearchService.showPopup(clubSearchData, callbackParams);
  }

  onAddClubOKCallback(scope: any, club: Club) {
    const me = scope;
    me.clubService.upsert(club)
      .pipe(first())
      .subscribe((savedClub: Club) => {
        me.updateClubInfoAndRefresh(savedClub.clubName, savedClub.id);
      });
  }
}
