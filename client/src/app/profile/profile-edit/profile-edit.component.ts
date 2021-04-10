import {ChangeDetectorRef, Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChange, SimpleChanges} from '@angular/core';
import {Profile} from '../profile';
import {StatesList} from '../../shared/states/states-list';
import {CountriesList} from '../../shared/countries-list';
import {DateUtils} from '../../shared/date-utils';
import {UsattRecordSearchCallbackData, UsattRecordSearchPopupService} from '../service/usatt-record-search-popup.service';
import {RecordSearchData} from '../usatt-record-search-popup/usatt-record-search-popup.component';
import {UsattPlayerRecord} from '../model/usatt-player-record.model';

@Component({
  selector: 'app-profile-edit',
  templateUrl: './profile-edit.component.html',
  styleUrls: ['./profile-edit.component.css']
})
export class ProfileEditComponent implements OnInit, OnChanges {

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

  constructor(private usattRecordSearchPopupService: UsattRecordSearchPopupService,
              private cdr: ChangeDetectorRef) {
    this.profile = new Profile();
    this.maxDateOfBirth = new Date();
    this.countries = CountriesList.getList();
    this.setZipCodeOptions ('US');
  }

  ngOnInit() {
  }

  onSave(formValues: any) {
    const profile: Profile = new Profile();
    profile.fromFormValues(formValues);
    this.saved.emit(profile);
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
//    console.log('updatedProfile' + JSON.stringify(updatedProfile));
    this.profile = updatedProfile;
    this.cdr.markForCheck();
  }
}
