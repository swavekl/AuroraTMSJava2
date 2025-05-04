import {Component, Inject, ViewEncapsulation} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';

@Component({
  selector: 'app-html-content-popup',
  templateUrl: './html-content-popup.component.html',
  styleUrl: './html-content-popup.component.scss',
  encapsulation: ViewEncapsulation.None
})
export class HtmlContentPopupComponent {

  title: string;

  message: string;

  contentAreaHeight: any;

  public OK = 'ok';

  constructor(public dialogRef: MatDialogRef<HtmlContentPopupComponent>,
              @Inject(MAT_DIALOG_DATA) public data: HtmlContentPopupData) {
    this.title = (data?.title) ? data.title : 'Information';
    this.message = data?.message;
    this.contentAreaHeight = (data.contentAreaHeight !== undefined) ? data.contentAreaHeight : '80px';
  }

  onOk(): void {
    this.dialogRef.close(this.OK);
  }
}

export interface HtmlContentPopupData {
  title: string;
  message: string;
  contentAreaHeight: string;
}

