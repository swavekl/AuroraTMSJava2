import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';

@Component({
    template: `
    <h2 mat-dialog-title>{{title}}</h2>
    <mat-dialog-content>
      <div fxFlexFill>
        <span>{{erroMessage}}</span>
      </div>
    </mat-dialog-content>
    <mat-dialog-actions>
      <div fxFlex="row" fxLayoutAlign="end start" style="width: 100%; padding-right: 10px;">
        <button mat-raised-button cdkFocusInitial type="button" (click)="onClose()" color="primary">Close</button>
      </div>
    </mat-dialog-actions>
  `,
    styles: [],
    standalone: false
})
export class ErrorMessagePopupComponent implements OnInit {

  erroMessage: string;
  title: string;

  constructor(public dialogRef: MatDialogRef<ErrorMessagePopupComponent>,
              @Inject(MAT_DIALOG_DATA) public data: ErrorMessagePopupData) {
    this.erroMessage = data?.errorMessage;
    this.title = data?.title;
    this.title = (this.title == null) ? "Error" : this.title;
  }

  ngOnInit(): void {
  }

  onClose() {
    this.dialogRef.close();
  }
}

/**
 * data to pass data to dialog
 */
export interface ErrorMessagePopupData {
  errorMessage: string;
  title: string;
}

