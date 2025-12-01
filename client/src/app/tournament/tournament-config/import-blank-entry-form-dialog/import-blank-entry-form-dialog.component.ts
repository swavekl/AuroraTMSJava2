import {Component, Inject, OnDestroy} from '@angular/core';
import {ImportProgressInfo} from '../../tournament-entry/model/import-progress-info.model';
import {interval, Subject} from 'rxjs';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {TournamentImportService} from '../../tournament-entry/service/tournament-import.service';
import {ErrorMessagePopupService} from '../../../shared/error-message-dialog/error-message-popup.service';
import {switchMap, takeUntil, tap} from 'rxjs/operators';

/**
 * Imports tournament and event information from a blank entry form PDF
 */
@Component({
  selector: 'app-import-blank-entry-form-dialog',
  standalone: false,
  templateUrl: './import-blank-entry-form-dialog.component.html',
  styleUrl: './import-blank-entry-form-dialog.component.scss'
})
export class ImportBlankEntryFormDialogComponent implements OnDestroy {
  protected importStarted: boolean;
  protected importProgressInfo: ImportProgressInfo = new ImportProgressInfo();

  private destroy$ = new Subject<void>();
  elapsedSeconds: number = 0;
  elapsedTimeIntervalId: any;

  selectedTargetTournamentId: number;
  blankEntryFormPDFURL: string;

  constructor(public dialogRef: MatDialogRef<ImportBlankEntryFormDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private tournamentImportService: TournamentImportService,
              private errorMessagePopupService: ErrorMessagePopupService) {
  }

  protected onBlankEntryFormUploadFinished(downloadUrl: string) {
    this.blankEntryFormPDFURL = downloadUrl.substring(downloadUrl.indexOf('path=') + 'path='.length);
  }

  protected getPDFStoragePath() {
    return 'tournament/blankentryform';
  }

  onCancel() {
    this.dialogRef.close({action: 'cancel'});
  }

  onOk() {
    const result = {
      action: 'ok',
      tournamentId: this.selectedTargetTournamentId
    };
    this.dialogRef.close(result);
  }

  isViewTournamentDisabled() {
    return this.importStarted || this.importProgressInfo.status != 'COMPLETED';
  }

  onViewTournament() {
    const result = {
      action: 'view',
      tournamentId: this.selectedTargetTournamentId
    };
    this.dialogRef.close(result);
  }

  formatTime(totalSeconds: number): string {
    if (this.elapsedSeconds != 0) {
      const hours = Math.floor(totalSeconds / 3600);
      const minutes = Math.floor((totalSeconds % 3600) / 60);
      const seconds = totalSeconds % 60;

      const pad = (num: number) => num.toString().padStart(2, '0');
      return `Elapsed Time: ${pad(hours)}:${pad(minutes)}:${pad(seconds)}`;
    } else {
      return '';
    }
  }

  protected startImport() {
    this.startTimer();
    this.tournamentImportService.importTournamentConfigurationFromPDF(this.blankEntryFormPDFURL)
      .subscribe({
        next: (importProgressInfo: ImportProgressInfo) => {
          if (importProgressInfo != null) {
            const jobId = importProgressInfo.jobId;
            this.startPolling(jobId);
          }
        },
        error: (error: any) => {
          const errorMessage: string = (error.error?.message) ?? error.message;
          this.errorMessagePopupService.showError(errorMessage, null, null, 'Error importing tournament');
        },
        complete: () => {
        }
      });
  }

  private startPolling(jobId: string): void {
    // Poll every 3 seconds
    interval(3000)
      .pipe(
        takeUntil(this.destroy$),
        switchMap(() => this.tournamentImportService.getImportStatus(jobId)),
        tap((importProgressInfo: ImportProgressInfo) => {
          this.importProgressInfo = importProgressInfo;
          if (importProgressInfo.status === 'COMPLETED' || importProgressInfo.status === 'FAILED') {
            console.log('importProgressInfo',importProgressInfo);
            if (importProgressInfo.tournamentId != 0) {
              this.selectedTargetTournamentId = importProgressInfo.tournamentId;
            }
            this.stopPolling();
          }
        })
      ).subscribe();
  }

  private stopPolling(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.stopTimer();
  }

  startTimer() {
    if (!this.importStarted) {
      this.importStarted = true;
      this.elapsedSeconds = 0;
      this.elapsedTimeIntervalId = setInterval(() => {
        this.elapsedSeconds++;
      }, 1000);
    }
  }

  stopTimer() {
    if (this.importStarted) {
      this.importStarted = false;
      clearInterval(this.elapsedTimeIntervalId);
    }
  }

  ngOnDestroy(): void {
    this.stopPolling();
  }

  protected isStartImportDisabled() {
    return this.importStarted
      || this.importProgressInfo.status == 'COMPLETED'
      || this.importProgressInfo.status == 'FAILED';
  }

  protected isNextDisabled() {
    return !(this.blankEntryFormPDFURL != null && this.blankEntryFormPDFURL.endsWith(".pdf"));
  }
}
