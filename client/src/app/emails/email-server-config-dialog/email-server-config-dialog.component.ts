import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {EmailServerConfiguration} from '../model/email-server-configuration.model';

@Component({
  selector: 'app-email-server-config-dialog',
  templateUrl: './email-server-config-dialog.component.html',
  styleUrls: ['./email-server-config-dialog.component.scss']
})
export class EmailServerConfigDialogComponent {

  public emailServerConfiguration: EmailServerConfiguration;

  public validHostname: '^(([a-zA-Z]|[a-zA-Z][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z]|[A-Za-z][A-Za-z0-9\\-]*[A-Za-z0-9])$';

  constructor(private dialogRef: MatDialogRef<EmailServerConfigDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any) {
    this.emailServerConfiguration = data;
  }

  onCancel() {
    this.dialogRef.close({action: 'cancel', config: null});
  }

  onSave(config: EmailServerConfiguration ) {
    this.dialogRef.close({action: 'ok', config: config});
  }

  onSendTestEmail() {
    console.log('Sending test email');
  }
}
