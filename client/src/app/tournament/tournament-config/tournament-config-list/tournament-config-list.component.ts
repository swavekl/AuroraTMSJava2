import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Tournament} from '../tournament.model';
import {ConfirmationPopupComponent} from '../../../shared/confirmation-popup/confirmation-popup.component';
import {MatDialog} from '@angular/material/dialog';
import {ImportTournamentDialogComponent} from '../import-tournament-dialog/import-tournament-dialog.component';
import {ImportBlankEntryFormDialogComponent} from '../import-blank-entry-form-dialog/import-blank-entry-form-dialog.component';

@Component({
    selector: 'app-tournament-config-list',
    templateUrl: './tournament-config-list.component.html',
    styleUrls: ['./tournament-config-list.component.scss'],
    standalone: false
})
export class TournamentConfigListComponent implements OnInit {

  @Input()
  tournaments: Tournament[];

  @Output()
  add: EventEmitter<any> = new EventEmitter<any>();

  @Output()
  delete: EventEmitter<number> = new EventEmitter<number>();

  @Output()
  refresh: EventEmitter<any> = new EventEmitter<any>();

  constructor(private dialog: MatDialog) {
  }

  ngOnInit(): void {
  }

  addTournament() {
    this.add.emit('add');
  }

  deleteTournament(tournament: Tournament): void {
    if (tournament.numEntries === 0) {
      const config = {
        width: '450px', height: '200px', data: {
          message: `Are you sure you want to delete ${tournament.name} tournament?`,
        }
      };
      const dialogRef = this.dialog.open(ConfirmationPopupComponent, config);
      dialogRef.afterClosed().subscribe(result => {
        if (result === 'ok') {
          this.delete.emit(tournament.id);
        }
      });
    } else {
      const config = {
        width: '450px', height: '210px', data: {
          message: `This tournament has ${tournament.numEntries} entries and can't be deleted.  Please remove the entries and then you can delete the tournament.`,
          showCancel: false, okText: 'Close', contentAreaHeight: 80
        }
      };
      const dialogRef = this.dialog.open(ConfirmationPopupComponent, config);
      dialogRef.afterClosed().subscribe(result => {
      });
    }
  }

  onImportFromOmnipong() {
    const config = {
      width: '750px', height: '520px', data: {
        existingTournaments: this.tournaments
      }
    };
    const dialogRef = this.dialog.open(ImportTournamentDialogComponent, config);
    const subscription = dialogRef.afterClosed().subscribe(result => {
      if (result.action === 'ok' || result.action === 'view') {
        this.refresh.emit(result);
      }
    });

  }

  onImportFromBlankEntryFormPDF() {
    const config = {
      width: '750px', height: '520px', data: {
        existingTournaments: this.tournaments
      }
    };
    const dialogRef = this.dialog.open(ImportBlankEntryFormDialogComponent, config);
    const subscription = dialogRef.afterClosed().subscribe(result => {
      if (result.action === 'ok' || result.action === 'view') {
        this.refresh.emit(result);
      }
    });

  }
}
