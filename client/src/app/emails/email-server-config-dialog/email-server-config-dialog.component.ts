import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {EmailServerConfiguration} from '../model/email-server-configuration.model';
import {EmailSenderService} from '../service/email-sender.service';
import {first} from 'rxjs/operators';

@Component({
  selector: 'app-email-server-config-dialog',
  templateUrl: './email-server-config-dialog.component.html',
  styleUrls: ['./email-server-config-dialog.component.scss']
})
export class EmailServerConfigDialogComponent {

  public emailServerConfiguration: EmailServerConfiguration;

  public validHostname: '^(([a-zA-Z]|[a-zA-Z][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z]|[A-Za-z][A-Za-z0-9\\-]*[A-Za-z0-9])$';

  errorMessage: string;
  isError: boolean;
  testing: boolean;

  constructor(private dialogRef: MatDialogRef<EmailServerConfigDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private emailService: EmailSenderService) {
    this.emailServerConfiguration = data;
    this.testing = false;
    this.isError = false;
  }

  onCancel() {
    this.dialogRef.close({action: 'cancel', config: null});
  }

  onSave(config: EmailServerConfiguration ) {
    this.dialogRef.close({action: 'ok', config: config});
  }

  onSendTestEmail(config: EmailServerConfiguration ) {
    this.errorMessage = null;
    this.isError = false;
    this.testing = true;
    this.emailService.sendTestEmail(config)
      .pipe(first())
      .subscribe(
        {
          next: (value) => {
            console.log('value', value);
            if (value) {
              this.errorMessage = "Email sent successfully.  Please check your inbox.";
            }
            this.testing = false;
          },
          error: (error: any) => {
            console.log('error', error);
            this.errorMessage = error.error?.message ?? error.message;
            this.isError = true;
            this.testing = false;
          },
          complete: () => { }
        });
  }

  isErrorTesting() {
    return this.isError;
  }
}
