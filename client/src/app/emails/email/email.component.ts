import {Component, EventEmitter, Inject, Input, Output, signal} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogConfig, MatDialogRef} from '@angular/material/dialog';
import {EmailServerConfigDialogComponent} from '../email-server-config-dialog/email-server-config-dialog.component';
import {PaymentDialogComponent} from '../../account/payment-dialog/payment-dialog.component';
import {EmailServerConfiguration} from '../model/email-server-configuration.model';
import {CallbackData} from '../../account/model/callback-data';

@Component({
  selector: 'app-email',
  templateUrl: './email.component.html',
  styleUrls: ['./email.component.scss']
})
export class EmailComponent {

  @Input()
  public tournamentName: string;

  @Input()
  public emailServerConfiguration: EmailServerConfiguration;

  @Input()
  public emailAddresses: string;

  @Output()
  private eventEmitter: EventEmitter<any> = new EventEmitter<any>();

  @Output()
  private emailConfigSave: EventEmitter<EmailServerConfiguration> = new EventEmitter<EmailServerConfiguration>();

  constructor(private dialog: MatDialog) {

  }

  getAllEmails() {
    this.eventEmitter.emit('getemails');
  }

  configureServer() {
    const config: MatDialogConfig = {
      width: '460px', height: '320px', data: this.emailServerConfiguration
    };
    // save the scope because it is wiped out in the component
    // so that it is not sent into the http service
    // const callbackScope = callbackData.callbackScope;
    const dialogRef = this.dialog.open(EmailServerConfigDialogComponent, config);
    dialogRef.afterClosed().subscribe(result => {
      if (result.action === 'ok') {
        this.onEmailConfigSave(result.config);
      } else {
        this.onEmailConfigCanceled();
      }
    });
  }

  onEmailConfigSave(config: EmailServerConfiguration) {
    this.emailConfigSave.emit(config);
  }

  onEmailConfigCanceled() {

  }

  back() {
    this.eventEmitter.emit('back');
  }
}
