import {Component, Inject, OnDestroy, OnInit} from '@angular/core';
import {Tournament} from '../tournament.model';
import {ImportProgressInfo} from '../../tournament-entry/model/import-progress-info.model';
import {interval, Subject} from 'rxjs';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {TournamentImportService} from '../../tournament-entry/service/tournament-import.service';
import {ErrorMessagePopupService} from '../../../shared/error-message-dialog/error-message-popup.service';
import {map, switchMap, takeUntil, tap} from 'rxjs/operators';
import {MatSelectChange} from '@angular/material/select';
import {ImportTournamentRequest} from '../../tournament-entry/model/import-tournament-request.model';
import {StatesList} from '../../../shared/states/states-list';
import {AuthenticationService} from '../../../user/authentication.service';

/**
 * Imports tournament definition
 */
@Component({
  selector: 'app-import-tournament-dialog',
  standalone: false,
  templateUrl: './import-tournament-dialog.component.html',
  styleUrl: './import-tournament-dialog.component.scss'
})
export class ImportTournamentDialogComponent implements OnInit, OnDestroy {
  // all tournaments from Omnipong tournament list
  allTournamentsToImport: ImportTournamentRequest[];

  // filtered for one state
  filteredTournamentsToImport: ImportTournamentRequest[];

  // this tournament director's existing tournaments
  existingTournaments: Tournament[];

  // to help find the tournament filter it by state
  statesOrRegions: string[];

  selectedTournamentUrl: string;
  selectedTargetTournamentId: number = 0;
  private selectedSourceTournament: ImportTournamentRequest;

  importStarted: boolean = false;

  importProgressInfo: ImportProgressInfo = new ImportProgressInfo();

  private destroy$ = new Subject<void>();
  elapsedSeconds: number = 0;
  elapsedTimeIntervalId: any;
  stateFilter: string;

  private readonly USATT_EVENTS = 'USATT Events';

  constructor(public dialogRef: MatDialogRef<ImportTournamentDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private tournamentImportService: TournamentImportService,
              private errorMessagePopupService: ErrorMessagePopupService,
              private authenticationService: AuthenticationService) {
    this.existingTournaments = data.existingTournaments;
  }

  ngOnInit(): void {
    this.tournamentImportService.listTournaments()
      .pipe(
        map(tournamentsToImport => {
          this.allTournamentsToImport = tournamentsToImport;
          this.statesOrRegions = this.extractUniqueStateList(tournamentsToImport);

          // set state filter to the current user's state
          const currentUserStateAbbreviation = this.authenticationService.getCurrentUserState();
          const countryStatesList = StatesList.getCountryStatesList(this.authenticationService.getCurrentUserCountry());
          const stateNameAndAbbreviation = countryStatesList.find(
            state => state.abbreviation === currentUserStateAbbreviation);
          this.stateFilter = (stateNameAndAbbreviation != null)
            ? stateNameAndAbbreviation.name
            : countryStatesList[0].name;
          console.log('stateFilter', this.stateFilter);
          this.filterByState(this.stateFilter);
        }))
      .subscribe();
  }

  /**
   *
   * @param tournamentsToImport
   * @private
   */
  private extractUniqueStateList(tournamentsToImport: ImportTournamentRequest[]): string [] {
    // get unique list of states and regions
    let statesOrRegions: string[] = [this.USATT_EVENTS];
    tournamentsToImport.forEach(tournament => {
      console.log('tournament state', tournament.tournamentState);
      const state = (tournament.tournamentState.length == 2) ? this.USATT_EVENTS : tournament.tournamentState;
      if (!statesOrRegions.includes(state)) {
        statesOrRegions.push(tournament.tournamentState);
      }
    });
    // remove USATT events and sort the remaining states
    statesOrRegions.splice(0, 1);
    statesOrRegions.sort((state1: string, state2: string) => {
      return state1.localeCompare(state2);
    });
    // Add the removed item to the beginning
    statesOrRegions.unshift(this.USATT_EVENTS);
    return statesOrRegions;
  }

  onCancel() {
    this.dialogRef.close({action: 'cancel'});
  }

  onOk() {
    const result = {
      action: 'ok'
    };
    this.dialogRef.close(result);
  }

  onViewTournament() {
    const result = {
      action: 'view',
      tournamentId: this.selectedTargetTournamentId
    };
    this.dialogRef.close(result);
  }

  startImport() {
    this.startTimer();
    // convert 'Illinois' to 'IL'
    const tournamentState = StatesList.convertToAbbreviation(this.selectedSourceTournament.tournamentState,
      this.authenticationService.getCurrentUserCountry() || 'US');
    const importTournamentRequest: ImportTournamentRequest = {
      ...this.selectedSourceTournament,
      tournamentState: tournamentState
    };
    this.tournamentImportService.importTournamentConfiguration(importTournamentRequest)
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
            if (this.selectedTargetTournamentId == 0 && importProgressInfo.tournamentId != 0) {
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

  ngOnDestroy(): void {
    this.stopPolling();
  }

  onTournamentSelected($event: MatSelectChange<any>) {
    const playersURL = $event.value;
    for (let i = 0; i < this.allTournamentsToImport.length; i++) {
      const tournamentToImport = this.allTournamentsToImport[i];
      if (playersURL === tournamentToImport.playersUrl) {
        this.selectedSourceTournament = tournamentToImport;
        break;
      }
    }

    // attempt to match source to target tournament by name
    for (let i = 0; i < this.existingTournaments.length; i++) {
      const existingTournament = this.existingTournaments[i];
      if (existingTournament.name == this.selectedSourceTournament.tournamentName) {
        this.selectedTargetTournamentId = existingTournament.id;
      }
    }
  }

  startTimer() {
    if (!this.importStarted) {
      this.importStarted = true;
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

  formatTime(totalSeconds: number): string {
    if (this.importStarted) {
      const hours = Math.floor(totalSeconds / 3600);
      const minutes = Math.floor((totalSeconds % 3600) / 60);
      const seconds = totalSeconds % 60;

      const pad = (num: number) => num.toString().padStart(2, '0');
      return `Elapsed Time: ${pad(hours)}:${pad(minutes)}:${pad(seconds)}`;
    } else {
      return '';
    }
  }

  isStartImportDisabled() {
    return this.importStarted
      || this.importProgressInfo.status == 'COMPLETED'
      || this.importProgressInfo.status == 'FAILED';
  }

  isViewTournamentDisabled() {
    return this.importStarted || this.importProgressInfo.status != 'COMPLETED';
  }

  onStateFilterChanged($event: MatSelectChange<any>) {
    const stateOrRegion = $event.value;
    this.filterByState(stateOrRegion);
  }

  filterByState(stateOrRegion: string) {
    console.log('filtering by ', stateOrRegion);
    const filterToUse = stateOrRegion.length == 2 ? this.USATT_EVENTS : stateOrRegion;
    this.filteredTournamentsToImport = this.allTournamentsToImport.filter(
      value => value.tournamentState === filterToUse);
  }
}
