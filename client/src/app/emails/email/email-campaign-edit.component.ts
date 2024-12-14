import {Component, ElementRef, EventEmitter, Input, Output, SimpleChanges, ViewChild} from '@angular/core';
import {EmailCampaign, Recipient} from '../model/email-campaign.model';
import {TournamentEvent} from '../../tournament/tournament-config/tournament-event.model';
import {MatSnackBar} from '@angular/material/snack-bar';
import {MatCheckboxChange} from '@angular/material/checkbox';

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

  constructor(private snackBar: MatSnackBar) {

  }

  onSendEmails() {
    this.eventEmitter.emit({action: 'sendemails', value: this.emailCampaign});
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
      excludeRegistered: false
    };
    this.emitFilterEvent();
  }

  onAllRecipientsChanged(event: MatCheckboxChange) {
    this.emailCampaign = {
      ...this.emailCampaign,
      recipientFilters: [],
      removedRecipients: [],
      allRecipients: event.checked,
      excludeRegistered: !event.checked ? false : this.emailCampaign.excludeRegistered
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


  private emitFilterEvent() {
    this.eventEmitter.emit({action: 'filter',
      recipientFilters: this.emailCampaign.recipientFilters,
      removedRecipients: this.emailCampaign.removedRecipients,
      allRecipients: this.emailCampaign.allRecipients,
      excludeRegistered: this.emailCampaign.excludeRegistered
    });
  }

  onRemoveRecipient() {
    if (this.selectedRecipient != null) {
      this.filteredRecipients = this.filteredRecipients.filter((recipient: Recipient) => {
        return (recipient.emailAddress != this.selectedRecipient?.emailAddress)
      });

      let modifiedRecipients: Recipient[] = this.emailCampaign.removedRecipients || [];
      modifiedRecipients.push(this.selectedRecipient);
      modifiedRecipients.sort((recipient1: Recipient, recipient2: Recipient) => {
        const fullName1 = this.getFullName(recipient1);
        const fullName2 = this.getFullName(recipient2);
        return fullName1.localeCompare(fullName2)
      });
      this.emailCampaign.removedRecipients = modifiedRecipients;
      this.selectedRecipient = null;
    }
  }

  public getFullName(recipient: Recipient) {
    return `${recipient.lastName}, ${recipient.firstName}`;
  }

  onRestoreRecipient() {
    if (this.removedRecipient != null) {
      // remove from list
      this.emailCampaign.removedRecipients = this.emailCampaign.removedRecipients.filter((recipient: Recipient) => {
        return (recipient.emailAddress != this.removedRecipient?.emailAddress)
      });

      // add to the other list and sort
      let modifiedRecipients: Recipient[] = this.filteredRecipients || [];
      modifiedRecipients.push(this.removedRecipient);
      modifiedRecipients.sort((recipient1: Recipient, recipient2: Recipient) => {
        const fullName1 = this.getFullName(recipient1);
        const fullName2 = this.getFullName(recipient2);
        return fullName1.localeCompare(fullName2)
      });
      this.filteredRecipients = modifiedRecipients;
      this.removedRecipient = null;
    }
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
          clipboardText += `${recipient.firstName}, ${recipient.lastName}, ${recipient.emailAddress}\n`;
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
}
