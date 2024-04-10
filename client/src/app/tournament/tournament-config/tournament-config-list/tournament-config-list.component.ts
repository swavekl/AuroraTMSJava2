import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Tournament} from '../tournament.model';
import {ConfirmationPopupComponent} from '../../../shared/confirmation-popup/confirmation-popup.component';
import {MatDialog} from '@angular/material/dialog';

@Component({
  selector: 'app-tournament-config-list',
  templateUrl: './tournament-config-list.component.html',
  styleUrls: ['./tournament-config-list.component.scss']
})
export class TournamentConfigListComponent implements OnInit {

  @Input()
  tournaments: Tournament[];

  @Output()
  add: EventEmitter<any> = new EventEmitter<any>();

  @Output()
  delete: EventEmitter<number> = new EventEmitter<number>();

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
}
