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
    // const clone = (data) ?? {...data};
    // const clone = (data) ?? JSON.parse(JSON.stringify(data));
    this.emailServerConfiguration = data;
    console.log('got email configuration for editing', this.emailServerConfiguration);
  }

  onCancel() {
    this.dialogRef.close({action: 'cancel', config: null});
  }

  onSave(config: EmailServerConfiguration ) {
    // const fullConfig: EmailServerConfiguration = {...config, id: this.emailServerConfiguration.id};
    // this.emailServerConfiguration = fullConfig;
    this.dialogRef.close({action: 'ok', config: config});
  }

  onSendTestEmail() {
    console.log('Sending test email');
  }
}
