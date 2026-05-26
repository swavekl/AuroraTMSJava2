import {Component, Inject, ViewEncapsulation} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

@Component({
    selector: 'app-html-content-popup',
    templateUrl: './html-content-popup.component.html',
    styleUrl: './html-content-popup.component.scss',
    encapsulation: ViewEncapsulation.None,
    standalone: false
})
export class HtmlContentPopupComponent {

  title: string;

  public message: SafeHtml;

  contentAreaHeight: any;

  public OK = 'ok';
  public Cancel = 'cancel';

  showOK: boolean = true;
  showCancel: boolean = false;
  cancelText: string;
  okText: string;

  constructor(
              public dialogRef: MatDialogRef<HtmlContentPopupComponent>,
              @Inject(MAT_DIALOG_DATA) public data: HtmlContentPopupData,
              private sanitizer: DomSanitizer) {
    this.title = (data?.title) ? data.title : 'Information';
    this.message = this.sanitizer.bypassSecurityTrustHtml(data?.message);
    this.contentAreaHeight = (data.contentAreaHeight !== undefined) ? data.contentAreaHeight : '80px';
    this.showOK = (data.showOK !== undefined) ? data.showOK : true;
    this.showCancel = (data.showCancel !== undefined) ? data.showCancel : false;
    this.okText = (data.okText !== undefined) ? data.okText : 'Close';
    this.cancelText = (data.cancelText !== undefined) ? data.cancelText : 'Cancel';
  }

  onOk(): void {
    this.dialogRef.close(this.OK);
  }

  protected onCancel() {
    this.dialogRef.close(this.Cancel);
  }
}

export interface HtmlContentPopupData {
  title: string;
  message: string;
  contentAreaHeight: string;
  showOK: boolean;
  showCancel: boolean;
  cancelText: string;
  okText: string;
}

