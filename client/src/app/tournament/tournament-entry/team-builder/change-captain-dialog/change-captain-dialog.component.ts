import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import {TeamMember} from '../../model/team-member.model';

@Component({
  selector: 'app-change-captain-dialog',
  templateUrl: './change-captain-dialog.component.html',
  standalone: false,
  styleUrls: ['./change-captain-dialog.component.scss']
})
export class ChangeCaptainDialogComponent {
  selectedProfileId: string;

  constructor(
    public dialogRef: MatDialogRef<ChangeCaptainDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { members: TeamMember[] }
  ) {
    // Pre-select the current captain
    const currentCaptain = data.members.find(m => m.captain);
    this.selectedProfileId = currentCaptain ? currentCaptain.profileId : '';
  }

  isSameCaptain(): boolean {
    const current = this.data.members.find(m => m.captain);
    return current?.profileId === this.selectedProfileId;
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onConfirm(): void {
    this.dialogRef.close(this.selectedProfileId);
  }
}
