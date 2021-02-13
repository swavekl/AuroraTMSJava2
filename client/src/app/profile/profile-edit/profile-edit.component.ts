import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChange, SimpleChanges} from '@angular/core';
import {Profile} from '../profile';
import {StatesList} from '../../shared/states/states-list';
import {CountriesList} from '../../shared/countries-list';
import {DateUtils} from '../../shared/date-utils';

@Component({
  selector: 'app-profile-edit',
  templateUrl: './profile-edit.component.html',
  styleUrls: ['./profile-edit.component.css']
})
export class ProfileEditComponent implements OnInit, OnChanges {

  // this is what we edit
  @Input() profile: Profile;

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

  constructor() {
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
}
