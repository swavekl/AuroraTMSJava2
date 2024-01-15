import {Component, ElementRef, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {EmailCampaign} from '../model/email-campaign.model';

@Component({
  selector: 'app-email-campaign-edit',
  templateUrl: './email-campaign-edit.component.html',
  styleUrls: ['./email-campaign-edit.component.scss']
})
export class EmailCampaignEditComponent {

  @Input()
  public emailCampaign: EmailCampaign;

  @Input()
  public tournamentName: string;

  @Output()
  private eventEmitter: EventEmitter<any> = new EventEmitter<any>();

  @ViewChild('subjectCtrl')
  private subjectCtrl: ElementRef<HTMLInputElement>;

  @ViewChild('bodyCtrl')
  private bodyCtrl: ElementRef<HTMLTextAreaElement>;

  private selectedFieldName: string;

  constructor() {

  }

  onSendEmails() {
    this.eventEmitter.emit({action: 'sendemails', value: null});
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
}
