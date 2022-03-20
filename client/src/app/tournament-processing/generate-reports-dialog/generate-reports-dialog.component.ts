import { Component, OnInit } from '@angular/core';
import {MatDialogRef} from '@angular/material/dialog';

/**
 * Dialog for collecting data needed to generate reports
 */
@Component({
  selector: 'app-generate-reports-dialog',
  templateUrl: './generate-reports-dialog.component.html',
  styleUrls: ['./generate-reports-dialog.component.scss']
})
export class GenerateReportsDialogComponent implements OnInit {

  public OK = 'ok';
  public CANCEL = 'cancel';

  public remarks: string;
  public ccLast4Digits: string;

  constructor(public dialogRef: MatDialogRef<GenerateReportsDialogComponent>) { }

  ngOnInit(): void {
  }

  onOk(formValues): void {
    this.dialogRef.close({
      action: this.OK, remarks: formValues.remarks, ccLast4Digits: formValues.ccLast4Digits
    });
  }

  onCancel(): void {
    this.dialogRef.close({action: this.CANCEL});
  }
}
