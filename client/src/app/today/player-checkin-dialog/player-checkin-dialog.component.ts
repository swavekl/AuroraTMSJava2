import {Component, Inject} from '@angular/core';
import {PlayerStatus} from '../model/player-status.model';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';

@Component({
  selector: 'app-player-checkin-dialog',
  templateUrl: './player-checkin-dialog.component.html',
  styleUrls: ['./player-checkin-dialog.component.scss']
})
export class PlayerCheckinDialogComponent {

  playerStatus: PlayerStatus;
  eventName: string;
  tournamentDay: number;
  fullName: string;

  constructor(public dialogRef: MatDialogRef<PlayerCheckinDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any) {
    this.playerStatus = data?.playerStatus;
    this.eventName = data?.eventName;
    this.tournamentDay = data?.tournamentDay;
    this.fullName = data?.fullName;
  }

  onPlayerStatusCanceled($event) {
    this.dialogRef.close({action: 'cancel', playerStatus: null});
  }

  onPlayerStatusSaved(updatedStatus: any) {
    this.dialogRef.close({action: 'ok', playerStatus: updatedStatus});
  }
}
