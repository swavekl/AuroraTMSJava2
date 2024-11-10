import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef, MatDialogTitle} from '@angular/material/dialog';
import {MatFormField} from '@angular/material/form-field';
import {FlexModule} from 'ng-flex-layout';
import {MatCheckbox} from '@angular/material/checkbox';
import {FormsModule} from '@angular/forms';
import {MatButton} from '@angular/material/button';
import {UserRoles} from '../../user/user-roles.enum';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'app-groups-dialog',
  standalone: true,
  imports: [
    MatDialogActions,
    MatDialogContent,
    MatFormField,
    FlexModule,
    MatCheckbox,
    FormsModule,
    MatButton,
    CommonModule,
    MatDialogTitle
  ],
  templateUrl: './groups-dialog.component.html',
  styleUrl: './groups-dialog.component.scss'
})
export class GroupsDialogComponent {

  public profileId: string;
  public username: string;
  public groups: string [];
  public allGroups: UserRoles [] = [];

  constructor(public dialogRef: MatDialogRef<GroupsDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any) {
    this.groups = data?.groups;
    this.profileId = data?.profile.userId;
    this.username = data?.profile.firstName + ' ' + data?.profile.lastName;
    this.allGroups = [
      UserRoles.ROLE_EVERYONE,
      UserRoles.ROLE_UMPIRES,
      UserRoles.ROLE_REFEREES,
      UserRoles.ROLE_DATA_ENTRY_CLERKS,
      UserRoles.ROLE_TOURNAMENT_DIRECTORS,
      UserRoles.ROLE_DIGITAL_SCORE_BOARDS,
      UserRoles.ROLE_MONITORS,
      UserRoles.ROLE_USATT_CLUB_MANAGERS,
      UserRoles.ROLE_USATT_SANCTION_COORDINATORS,
      UserRoles.ROLE_USATT_INSURANCE_MANAGERS,
      UserRoles.ROLE_USATT_PLAYER_MANAGERS,
      UserRoles.ROLE_USATT_MATCH_OFFICIALS_MANAGERS,
      UserRoles.ROLE_USATT_TOURNAMENT_MANAGERS,
      UserRoles.ROLE_ADMINS,
    ];
  }

  onCancel() {
    this.dialogRef.close({action: 'cancel'});
  }

  onSave(value: any) {
    console.log(value);
    this.dialogRef.close({action: 'ok', groups: value});
  }

  isChecked(group: UserRoles) {
    return this.groups.includes(group);
  }
}
