import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Profile} from '../profile';
import {StatesList} from '../../shared/states/states-list';

@Component({
  selector: 'app-profile-edit',
  templateUrl: './profile-edit.component.html',
  styleUrls: ['./profile-edit.component.css']
})
export class ProfileEditComponent implements OnInit {

  // this is what we edit
  @Input() profile: Profile;

  // save and cancel
  @Output() saved = new EventEmitter();
  @Output() canceled = new EventEmitter();

  // list of US states
  statesList: any [];

  constructor() {
    this.profile = new Profile();
    this.statesList = new StatesList().getList();
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
}
