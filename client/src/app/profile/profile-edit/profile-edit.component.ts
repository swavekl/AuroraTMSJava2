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

  constructor() {
    this.profile = new Profile();
    this.statesList = StatesList.getList();
    this.maxDateOfBirth = new Date();
    this.countries = CountriesList.getList();
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
    }
  }
}
