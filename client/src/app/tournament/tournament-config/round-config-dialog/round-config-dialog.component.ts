import {Component, Inject, Input} from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { TournamentEventRound } from '../model/tournament-event-round.model';

@Component({
  selector: 'app-round-config-dialog',
  standalone: false,
  templateUrl: './round-config-dialog.component.html',
  styleUrl: './round-config-dialog.component.scss'
})
export class RoundConfigDialogComponent {
  // Local copy to prevent immediate binding updates
  round: TournamentEventRound;

  days: any [] = [];

  startTimes: any [] = [];

  constructor(
    public dialogRef: MatDialogRef<RoundConfigDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public originalData: any
  ) {
    // Clone the data so we can cancel changes if needed
    this.round = { ...originalData.round };
    this.days = originalData.days;
    this.startTimes = originalData.startTimes;
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    this.dialogRef.close(this.round);
  }
}
