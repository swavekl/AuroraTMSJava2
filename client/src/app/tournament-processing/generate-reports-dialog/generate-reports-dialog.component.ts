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

  // flags to indicate which reports to generate
  public generateTournamentReport: boolean;
  public generateApplications: boolean;
  public generatePlayerList: boolean;
  public generateMatchResults: boolean;
  public generateMembershipList: boolean;
  public generateDeclarationOfCompliance: boolean;
  // State or regional championship ranking report
  public generateRankingReport: boolean;
  // tournament id assigned by USATT to this tournament
  public rankingReportTournamentId: number;

  constructor(public dialogRef: MatDialogRef<GenerateReportsDialogComponent>) { }

  ngOnInit(): void {
  }

  isOkEnabled(formValues: any) {
    return formValues.generateTournamentReport ||
      formValues.generateApplications ||
      formValues.generatePlayerList ||
      formValues.generateMatchResults ||
      formValues.generateMembershipList ||
      formValues.generateDeclarationOfCompliance ||
      formValues.generateRankingReport;
  }

  onOk(formValues): void {
    this.dialogRef.close({
      action: this.OK,
      remarks: formValues.remarks,
      ccLast4Digits: formValues.ccLast4Digits,
      generateTournamentReport: formValues.generateTournamentReport,
      generateApplications: formValues.generateApplications,
      generatePlayerList: formValues.generatePlayerList,
      generateMatchResults: formValues.generateMatchResults,
      generateMembershipList: formValues.generateMembershipList,
      generateDeclarationOfCompliance: formValues.generateDeclarationOfCompliance,
      generateRankingReport: formValues.generateRankingReport,
      rankingReportTournamentId: formValues.rankingReportTournamentId
    });
  }

  onCancel(): void {
    this.dialogRef.close({action: this.CANCEL});
  }
}
