import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';

@Component({
    selector: 'app-confirmation-popup',
    templateUrl: './confirmation-popup.component.html',
    styleUrls: ['./confirmation-popup.component.css'],
    standalone: false
})
export class ConfirmationPopupComponent implements OnInit {
  title: string;
  message: string;
  showOK: boolean;
  showCancel: boolean;
  contentAreaHeight: string;
  public OK = 'ok';
  public CANCEL = 'cancel';

  cancelText: string;
  okText: string;

  constructor(public dialogRef: MatDialogRef<ConfirmationPopupComponent>,
              @Inject(MAT_DIALOG_DATA) public data: ConfirmationPopupData) {
    this.title = (data?.title) ? data.title : 'Warning';
    this.message = data?.message;
    this.showOK = (data.showOK !== undefined) ? data.showOK : true;
    this.showCancel = (data.showCancel !== undefined) ? data.showCancel : true;
    this.okText = (data.okText !== undefined) ? data.okText : 'OK';
    this.cancelText = (data.cancelText !== undefined) ? data.cancelText : 'Cancel';
    this.contentAreaHeight = (data.contentAreaHeight !== undefined) ? data.contentAreaHeight : '80px';
  }

  ngOnInit(): void {
  }

  onOk(): void {
    this.dialogRef.close(this.OK);
  }

  onCancel(): void {
    this.dialogRef.close(this.CANCEL);
  }

}

/**
 * data to customize appearance of popup
 */
export interface ConfirmationPopupData {
  title: string;
  message: string;
  showOK: true;
  showCancel: true;
  contentAreaHeight: string; // e.g. 80px
  cancelText: string;
  okText: string;
}
