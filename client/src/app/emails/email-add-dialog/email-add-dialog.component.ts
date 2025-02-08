import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';

@Component({
  selector: 'app-email-add-dialog',
  templateUrl: './email-add-dialog.component.html',
  styleUrl: './email-add-dialog.component.scss'
})
export class EmailAddDialogComponent {

  htmlEmail: boolean;
  backgroundColor: string;
  textColor: string;
  linkColor: string;

  readonly HEX_COLOR_PATTERN = "^#([0-9A-Fa-f]{3}|[0-9A-Fa-f]{6})$"

  constructor(public dialogRef: MatDialogRef<EmailAddDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any) {
    this.htmlEmail = false;
    this.backgroundColor = '#ffffff';
    this.textColor = '#000000';
    this.linkColor = '#1212FF'
  }

  onCancel() {
    this.dialogRef.close({action: 'cancel', campaignInitData: null});
  }

  onAdd() {
    const campaignInitData: CampaignInitData = {
      htmlEmail: this.htmlEmail,
      backgroundColor: this.backgroundColor,
      textColor: this.textColor,
      linkColor: this.linkColor
    }
    this.dialogRef.close({action: 'ok', campaignInitData: campaignInitData });
  }
}

export interface CampaignInitData {
  htmlEmail: boolean;
  backgroundColor: string;
  textColor: string;
  linkColor: string;
}
