import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {DoublesPairInfo} from '../model/doubles-pair-info.model';

@Component({
  selector: 'app-doubles-pair-dialog',
  templateUrl: './doubles-pair-dialog.component.html',
  styleUrls: ['./doubles-pair-dialog.component.scss']
})
export class DoublesPairDialogComponent implements OnInit {

  // player A & B event entry ids
  playerAEventEntryId: number;
  playerBEventEntryId: number;

  // names and event entry ids for all unpaired players
  doublesPairingInfos: DoublesPairingInfo [] = [];

  constructor(public dialogRef: MatDialogRef<DoublesPairDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: DoublesPairingData) {
    this.doublesPairingInfos = data.doublesPairingInfos;
    this.doublesPairingInfos.sort((a: DoublesPairingInfo, b: DoublesPairingInfo) => {
      return a.playerName.localeCompare(b.playerName);
    });
  }

  ngOnInit(): void {
  }

  onCancel() {
    this.dialogRef.close({action: 'cancel'});
  }

  onOk() {
    const result = {
      action: 'ok',
      playerAEventEntryId: this.playerAEventEntryId,
      playerBEventEntryId: this.playerBEventEntryId
    };
    this.dialogRef.close(result);
  }
}

export class DoublesPairingData {
  doublesPairingInfos: DoublesPairingInfo[];
}

export class DoublesPairingInfo {
  eventEntryId: number;
  playerName: string;
}
