import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';

@Component({
  selector: 'app-default-player-phone-dialog',
  templateUrl: './default-player-phone-dialog.component.html',
  styleUrls: ['./default-player-phone-dialog.component.scss']
})
export class DefaultPlayerPhoneDialogComponent {
  doubles: boolean;
  // player / team letter
  playerALetter: string;
  playerBLetter: string;
  // singles

  playerAName: string;
  playerBName: string;
  // doubles
  teamAPlayer1: string;
  teamAPlayer2: string;
  teamBPlayer1: string;
  teamBPlayer2: string;

  // selected player / team
  defaultedPlayerIndex: string;

  constructor(public dialogRef: MatDialogRef<DefaultPlayerPhoneDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any) {
    this.doubles = data?.doubles;
    this.playerALetter = data?.playerALetter;
    this.playerBLetter = data?.playerBLetter;
    this.playerAName = data?.playerAName;
    this.playerBName = data?.playerBName;
    if (this.doubles) {
      const teamANames: string [] = this.playerAName.split('/');
      this.teamAPlayer1 = teamANames[0];
      this.teamAPlayer2 = teamANames[1] + ' long name very';
      const teamBNames: string [] = this.playerBName.split('/');
      this.teamBPlayer1 = teamBNames[0];
      this.teamBPlayer2 = teamBNames[1];
    }
  }

  onCancel() {
    this.dialogRef.close({action: 'cancel', defaultedPlayerIndex: null});
  }

  onSave() {
    this.dialogRef.close({action: 'save', defaultedPlayerIndex: Number(this.defaultedPlayerIndex)});
  }
}
