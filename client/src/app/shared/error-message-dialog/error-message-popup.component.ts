import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';

@Component({
  template: `
    <h2 mat-dialog-title>Error</h2>
    <mat-dialog-content>
      <p>{{erroMessage}}</p>
    </mat-dialog-content>
    <mat-dialog-actions>
      <button mat-button cdkFocusInitial type="button" (click)="onClose()">Close</button>
    </mat-dialog-actions>
  `,
  styles: [
  ]
})
export class ErrorMessagePopupComponent implements OnInit {

  erroMessage: string;

  constructor(public dialogRef: MatDialogRef<ErrorMessagePopupComponent>,
              @Inject(MAT_DIALOG_DATA) public data: ErrorMessagePopupData) {
    this.erroMessage = data?.errorMessage;
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
}

