import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';

@Component({
  selector: 'app-end-match-popup',
  templateUrl: './end-match-popup.component.html',
  styleUrl: './end-match-popup.component.scss'
})
export class EndMatchPopupComponent {
  endReason: string;
  winningSide: string;
  playerAName: string;
  playerBName: string;
  matchFinished: boolean;
  matchStarted: boolean;

  constructor(public dialogRef: MatDialogRef<EndMatchPopupComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any) {
    this.playerAName = data?.playerAName;
    this.playerBName = data?.playerBName;
    this.winningSide = data?.winningSide;
    this.matchFinished = data?.matchFinished;
    this.matchStarted = data?.matchStarted;
    this.endReason = this.matchFinished ? 'NormalEnd' : (!this.matchStarted ? 'WalkOver' : 'Injury');
  }

  onOk(){
    this.dialogRef.close({endReason: this.endReason, winningSide: this.winningSide});
  }
}
