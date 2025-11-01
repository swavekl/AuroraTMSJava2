import {Component, Inject, OnDestroy, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatSelectChange} from '@angular/material/select';
import {finalize, map, switchMap, takeUntil, tap} from 'rxjs/operators';
import {interval, Subject} from 'rxjs';
import {TournamentImportService} from '../service/tournament-import.service';
import {ImportEntriesRequest} from '../model/import-entries-request.model';
import {ImportTournamentRequest} from '../model/import-tournament-request.model';
import {ImportProgressInfo} from '../model/import-progress-info.model';
import {ErrorMessagePopupService} from '../../../shared/error-message-dialog/error-message-popup.service';
import {StepperSelectionEvent} from '@angular/cdk/stepper';

@Component({
  selector: 'app-omnipong-import-dialog',
  standalone: false,
  templateUrl: './import-tournament-entries-dialog.component.html',
  styleUrl: './import-tournament-entries-dialog.component.scss'
})
export class ImportTournamentEntriesDialogComponent implements OnInit, OnDestroy {
  // tournaments from Omnipong
  tournaments: ImportTournamentRequest[];

  selectedTournamentUrl: string;
  selectedTournamentName: string;
  selectedTargetTournamentId: number;
  private emailsFileRepoPath: string;

  importStarted: boolean = false;

  importProgressInfo: ImportProgressInfo = new ImportProgressInfo();

  private destroy$ = new Subject<void>();
  elapsedSeconds: number = 0;
  elapsedTimeIntervalId: any;
  protected playerAccountsCheckResults: string;
  protected isCheckingAccounts: boolean;

  constructor(public dialogRef: MatDialogRef<ImportTournamentEntriesDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private tournamentImportService: TournamentImportService,
              private errorMessagePopupService: ErrorMessagePopupService) {
    this.selectedTournamentName = data.sourceTournamentName;  // helps select the right source tournament
    this.selectedTargetTournamentId = data.targetTournamentId;
  }

  ngOnInit(): void {
    this.tournamentImportService.listTournaments()
      .pipe(
        map(tournaments => {
          this.tournaments = tournaments;
          this.selectedTournament();
        }))
      .subscribe();
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

  startImport() {
    this.startTimer();
    const importEntriesRequest: ImportEntriesRequest = {
      tournamentId: this.selectedTargetTournamentId,
      playersUrl: this.selectedTournamentUrl,
      emailsFileRepoPath: this.emailsFileRepoPath
    };
    this.tournamentImportService.importTournamentEntries(importEntriesRequest)
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
    interval(5000)
      .pipe(
        takeUntil(this.destroy$),
        switchMap(() => this.tournamentImportService.getImportStatus(jobId)),
        tap((importProgressInfo: ImportProgressInfo) => {
          this.importProgressInfo = importProgressInfo;
          if (importProgressInfo.status === 'COMPLETED' || importProgressInfo.status === 'FAILED') {
            if (this.isCheckingAccounts) {
              const profilesMissing = importProgressInfo.profilesMissing;
              this.playerAccountsCheckResults = `We found ${importProgressInfo.totalEntries} players who entered the tournament and ${profilesMissing} of them don't have accounts in this system.`;
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

  ngOnDestroy(): void {
    this.stopPolling();
  }

  private selectedTournament() {
    for (let i = 0; i < this.tournaments.length; i++) {
      const tournament = this.tournaments[i];
      if (this.selectedTournamentName === tournament.tournamentName) {
        this.selectedTournamentUrl = tournament.playersUrl;
        break;
      }
    }
  }

  onTournamentSelected($event: MatSelectChange<any>) {
    const playersURL = $event.value;
    for (let i = 0; i < this.tournaments.length; i++) {
      const tournament = this.tournaments[i];
      if (playersURL === tournament.playersUrl) {
        this.selectedTournamentName = tournament.tournamentName;
        this.selectedTargetTournamentId = tournament.tournamentId;
        break;
      }
    }
  }

  onEmailsUploadFinished(downloadUrl: string) {
    this.emailsFileRepoPath = downloadUrl.substring(downloadUrl.indexOf('path=') + 'path='.length);
  }

  getEmailsStoragePath() {
    return (this.selectedTargetTournamentId != null)
      ? `importtournament/${this.selectedTargetTournamentId}/emails`
      : null;
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
    if (this.importStarted || this.isCheckingAccounts) {
      this.importStarted = false;
      this.isCheckingAccounts = false;
      clearInterval(this.elapsedTimeIntervalId);
    }
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

  onStepChange(event: StepperSelectionEvent): void {
    if (event.previouslySelectedIndex === 0 && event.selectedIndex === 1) {
      this.playerAccountsCheckResults = '';
      this.isCheckingAccounts = true;
      const importEntriesRequest: ImportEntriesRequest = {
        tournamentId: this.selectedTargetTournamentId,
        playersUrl: this.selectedTournamentUrl,
        emailsFileRepoPath: null
      };
      console.log('checking accounts');
      this.tournamentImportService.checkAccounts(importEntriesRequest)
        .subscribe({
          next: (importProgressInfo: ImportProgressInfo) => {
            if (importProgressInfo != null) {
              this.importProgressInfo = importProgressInfo;
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
    } else if (event.previouslySelectedIndex === 1 && event.selectedIndex === 2) {
      // start the next page from 0% complete
      this.importProgressInfo = null;
    }
  }
}

