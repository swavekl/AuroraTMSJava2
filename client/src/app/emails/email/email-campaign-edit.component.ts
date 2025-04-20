import {Component, ElementRef, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {EmailCampaign, Recipient} from '../model/email-campaign.model';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatCheckboxChange} from '@angular/material/checkbox';
import {ConfirmationPopupComponent} from '../../shared/confirmation-popup/confirmation-popup.component';
import {MatDialog} from '@angular/material/dialog';
import {StatesList} from '../../shared/states/states-list';

@Component({
  selector: 'app-email-campaign-edit',
  templateUrl: './email-campaign-edit.component.html',
  styleUrls: ['./email-campaign-edit.component.scss']
})
export class EmailCampaignEditComponent  {

  @Input()
  public emailCampaign: EmailCampaign;

  @Input()
  public tournamentName: string;

  @Input()
  public tournamentEvents: TournamentEvent[] = [];

  @Input()
  filteredRecipients!: Recipient[] | null;

  @Output()
  private eventEmitter: EventEmitter<any> = new EventEmitter<any>();

  @ViewChild('subjectCtrl')
  private subjectCtrl: ElementRef<HTMLInputElement>;

  @ViewChild('bodyCtrl')
  private bodyCtrl: ElementRef<HTMLTextAreaElement>;

  // which of the two fields (subject or body) currently has focus and should receive inserted variable.
  private selectedFieldName: string;

  private selectedRecipient: Recipient;
  private removedRecipient: Recipient;

  // states to filter by if filtering not by event
  public statesList: any [];
  public readonly ALL_STATES: string= 'ALL';

  constructor(private snackBar: MatSnackBar,
              private dialog: MatDialog) {
    let countryCode = 'US';
    this.statesList = StatesList.getCountryStatesList(countryCode);
  }

  onSendEmails() {
    if (this.filteredRecipients?.length > 450) {
      const config = {
        width: '450px', height: '230px', data: {
          contentAreaHeight: 130, showCancel: false, okText: 'Close', title: 'Warning',
          message: `Your email provider account has a limit of 450 emails per day.
          You are trying to send ${this.filteredRecipients.length} emails so some emails would not be sent.
          Please reduce the list or split it into chunks by state AL - FL today, GA - MN tomorrow, etc.`
        }
      };
      const dialogRef = this.dialog.open(ConfirmationPopupComponent, config);
      dialogRef.afterClosed().subscribe(result => {
      });
    } else {
      this.eventEmitter.emit({action: 'sendemails', value: this.emailCampaign});
    }
  }

  onSendTestEmail() {
    this.eventEmitter.emit({action: 'sendtestemail', value: this.emailCampaign});
  }

  onCancel() {
    this.eventEmitter.emit({action: 'cancel', value: null});
  }

  onSave() {
    this.eventEmitter.emit({action: 'save', value: this.emailCampaign});
  }

  onInsertVariable(variableName: string) {
    const variableText: string = `\$\{${variableName}\}`;
    if (this.selectedFieldName === 'body') {
      const bodyTextArea: HTMLTextAreaElement = this.bodyCtrl?.nativeElement as HTMLTextAreaElement;
      if (bodyTextArea) {
        const selectionStart = bodyTextArea.selectionStart;
        this.emailCampaign.body = this.emailCampaign.body.substring(0, selectionStart)
          + variableText + this.emailCampaign.body.substring(selectionStart);
      }
    } else if (this.selectedFieldName === 'subject') {
      const subjectInput: HTMLInputElement = this.subjectCtrl?.nativeElement as HTMLInputElement;
      if (subjectInput) {
        const selectionStart = subjectInput.selectionStart;
        this.emailCampaign.subject = this.emailCampaign.subject.substring(0, selectionStart)
          + variableText + this.emailCampaign.subject.substring(selectionStart);
      }
    }
  }

  onSelectedSubject() {
    this.selectedFieldName = 'subject';
  }

  onSelectedBody() {
    this.selectedFieldName = 'body';
  }

  onUnselect() {
    this.selectedFieldName = null;
  }

  onSelectEvent(eventId: number) {
    if (eventId === 0) {
      this.emailCampaign.recipientFilters = [0];  // event 0 means all players
    } else {
      const allIndex: number = this.emailCampaign.recipientFilters.indexOf(0);
      if (allIndex != -1) {
        this.emailCampaign.recipientFilters = this.emailCampaign.recipientFilters.filter((e, i) => i !== allIndex);
      }
    }
    this.emailCampaign = {
      ...this.emailCampaign,
      recipientFilters: this.emailCampaign.recipientFilters,
      removedRecipients: this.emailCampaign.removedRecipients,
      allRecipients: false,
      excludeRegistered: false,
      stateFilters: []
    };
    this.emitFilterEvent();
  }

  onAllRecipientsChanged(event: MatCheckboxChange) {
    this.emailCampaign = {
      ...this.emailCampaign,
      recipientFilters: [],
      removedRecipients: [],
      allRecipients: event.checked,
      excludeRegistered: !event.checked ? false : this.emailCampaign.excludeRegistered,
      stateFilters: [this.ALL_STATES]
    };
    this.emitFilterEvent();
  }

  onExcludeRegisteredChanged(event: MatCheckboxChange) {
    this.emailCampaign = {
      ...this.emailCampaign,
      recipientFilters: [],
      removedRecipients: [],
      excludeRegistered: event.checked
    };
    this.emitFilterEvent();
  }

  onIncludeUploadedRecipients($event: MatCheckboxChange) {
    this.emailCampaign = {
      ...this.emailCampaign,
      recipientFilters: [],
      removedRecipients: [],
      includeUploadedRecipients: $event.checked
    };
    this.emitFilterEvent();

    if (this.emailCampaign.includeUploadedRecipients && !this.emailCampaign.uploadedRecipientsFile) {
      const config = {
        width: '450px', height: '230px', data: {
          message: `File was not uploaded for this email campaign.
          Please upload a comma separated values file with the following columns:
           lastName, firstName, emailAddress`, contentAreaHeight: '100px', showCancel: false
        }
      };
      const dialogRef = this.dialog.open(ConfirmationPopupComponent, config);
      dialogRef.afterClosed().subscribe(result => {
        if (result === 'ok') {
        }
      });
    }
  }

  onClickState(stateAbbreviation: string) {
    let stateFilters: string [] = this.emailCampaign.stateFilters || [];
    if (stateAbbreviation === this.ALL_STATES || stateFilters?.length === 0) {
      stateFilters = [this.ALL_STATES];
    } else {
      const allIndex: number = stateFilters.indexOf(this.ALL_STATES);
      if (allIndex != -1) {
        stateFilters = stateFilters.filter((e, i) => i !== allIndex);
      }
    }
    this.emailCampaign = {
      ...this.emailCampaign,
      stateFilters: stateFilters
    };
    this.emitFilterEvent();
  }

  private emitFilterEvent() {
    this.eventEmitter.emit({action: 'filter',
      recipientFilters: this.emailCampaign.recipientFilters,
      removedRecipients: this.emailCampaign.removedRecipients,
      allRecipients: this.emailCampaign.allRecipients,
      excludeRegistered: this.emailCampaign.excludeRegistered,
      stateFilters: this.emailCampaign.stateFilters,
      uploadedRecipientsFile: this.emailCampaign?.uploadedRecipientsFile,
      includeUploadedRecipients: this.emailCampaign?.includeUploadedRecipients
    });
  }

  private sortRecipients(modifiedRecipients: Recipient[]) {
    return modifiedRecipients.sort((recipient1: Recipient, recipient2: Recipient) => {
      const fullName1 = this.getFullName(recipient1);
      const fullName2 = this.getFullName(recipient2);
      return fullName1.localeCompare(fullName2);
    });
  }

  onRemoveRecipient(clickedRecipient: Recipient) {
    const recipientToRemove = (clickedRecipient != null) ? clickedRecipient : this.selectedRecipient;
    if (recipientToRemove != null) {
      this.filteredRecipients = this.filteredRecipients.filter((recipient: Recipient) => {
        return (recipient.emailAddress != recipientToRemove?.emailAddress)
      });

      let modifiedRecipients: Recipient[] = this.emailCampaign.removedRecipients || [];
      modifiedRecipients.push(recipientToRemove);
      modifiedRecipients = this.sortRecipients(modifiedRecipients);
      this.emailCampaign.removedRecipients = modifiedRecipients;
      this.selectedRecipient = null;
    }
  }

  getRecipientTooltip(recipient: Recipient): string {
    return recipient.emailAddress + ((recipient.state != null) ? ' (' + recipient.state + ')' : '');
  }

  public getFullName(recipient: Recipient) {
    return `${recipient.lastName}, ${recipient.firstName}`;
  }

  onRestoreRecipient(recipient: Recipient) {
    const recipientToRestore = (recipient != null) ? recipient : this.removedRecipient;
    if (recipientToRestore != null) {
      // remove from list
      this.emailCampaign.removedRecipients = this.emailCampaign.removedRecipients.filter((recipient: Recipient) => {
        return (recipient.emailAddress != recipientToRestore?.emailAddress)
      });

      // add to the other list and sort
      let modifiedRecipients: Recipient[] = this.filteredRecipients || [];
      modifiedRecipients.push(recipientToRestore);
      modifiedRecipients = this.sortRecipients(modifiedRecipients);
      this.filteredRecipients = modifiedRecipients;
      this.removedRecipient = null;
    }
  }

  onRemoveAllRecipients() {
    let modifiedRecipients: Recipient[] = this.emailCampaign.removedRecipients || [];
    modifiedRecipients = modifiedRecipients.concat(this.filteredRecipients);
    modifiedRecipients = this.sortRecipients(modifiedRecipients);
    this.filteredRecipients = [];
    this.emailCampaign = {
      ...this.emailCampaign,
      removedRecipients: modifiedRecipients
    };
  }

  onRestoreAllRecipients() {
    let modifiedRecipients: Recipient[] = this.filteredRecipients || [];
    modifiedRecipients = modifiedRecipients.concat(this.emailCampaign.removedRecipients);
    modifiedRecipients = this.sortRecipients(modifiedRecipients);
    this.filteredRecipients = modifiedRecipients;
    this.emailCampaign = {
      ...this.emailCampaign,
      removedRecipients: []
    };
  }


  onSelectedRecipientClick(recipient: Recipient) {
    this.selectedRecipient = recipient;
  }

  onRemovedRecipientClick(recipient: Recipient) {
    this.removedRecipient = recipient;
  }

  copyRecipients() {
    if (!navigator.clipboard) {
      let text = "Your browser doesn't have support for native clipboard.";
      this.snackBar.open(text, 'Close', {
        verticalPosition: 'top', duration: 3000
      });
    } else {
      let clipboardText = '';
      let recipientsCount = 0;
      if (this.filteredRecipients != null && this.filteredRecipients.length > 0) {
        for (const recipient of this.filteredRecipients) {
          clipboardText += `${recipient.firstName}, ${recipient.lastName}, ${recipient.emailAddress}, ${recipient.state}\n`;
        }
        recipientsCount = this.filteredRecipients.length;
      }

      navigator.clipboard.writeText(clipboardText);

      const statusText = `${recipientsCount} were copied to clipboard.`;
      this.snackBar.open(statusText, 'Close', {
        verticalPosition: 'top', duration: 2500
      });
    }
  }

  onRecipientsUploadFinished(downloadUrl: any) {
    const ratingsFileRepoPath: string = downloadUrl.substring(downloadUrl.indexOf("path=") + "path=".length);
    this.emailCampaign = {
      ...this.emailCampaign,
      uploadedRecipientsFile: ratingsFileRepoPath,
      includeUploadedRecipients: true
    };
    this.emitFilterEvent();
  }

  /**
   * gets name of subfolder in repository where the file should be stored
   */
  getRecipientsFileStoragePath(): string {
    return `emailcampaignrecipients/${this.emailCampaign.id}`;
  }
}
