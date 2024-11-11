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
  templateUrl: './roles-dialog.component.html',
  styleUrl: './roles-dialog.component.scss'
})
export class RolesDialogComponent {

  public profileId: string;
  public userFullName: string;
  public readonly allRoles: UserRoles [] = [
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
    UserRoles.ROLE_ADMINS
  ];
  public selectedRoles: boolean [] = []

  constructor(public dialogRef: MatDialogRef<RolesDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any) {
    this.profileId = data?.profile.userId;
    this.userFullName = data?.profile.firstName + ' ' + data?.profile.lastName;
    const userRoles = data?.roles;
    this.selectedRoles = new Array(this.allRoles.length);
    for (const role of this.allRoles) {
      this.selectedRoles[role] = userRoles.includes(role);
    }
  }

  onCancel() {
    this.dialogRef.close({action: 'cancel'});
  }

  onSave(formValues: any) {
    const updatedRoles = [];
    for (const roleName in formValues) {
      if (formValues[roleName]) {
        updatedRoles.push(roleName);
      }
    }
    this.dialogRef.close({action: 'ok', roles: updatedRoles});
  }
}
