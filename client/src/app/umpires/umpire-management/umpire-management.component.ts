import {Component, Input} from '@angular/core';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {AssignUmpiresDialogComponent} from '../assign-umpires-dialog/assign-umpires-dialog.component';
import {Personnel} from '../../tournament/tournament-config/model/personnel.model';

@Component({
  selector: 'app-umpire-management',
  templateUrl: './umpire-management.component.html',
  styleUrl: './umpire-management.component.scss'
})
export class UmpireManagementComponent {

  @Input()
  tournamentId: number;

  @Input()
  tournamentDay!: number;

  @Input()
  umpireList!: Personnel[];

  constructor(private linearProgressBarService: LinearProgressBarService,
              private dialog: MatDialog) {
  }

  onViewUmpireDetails(umpireProfileId: string) {

  }

  onAssignUmpire() {
    const config: MatDialogConfig = {
      width: '650px', height: '380px', data: {
        tournamentId: this.tournamentId,
        tournamentDay: this.tournamentDay,
        umpireList: this.umpireList
      }
    };
    const dialogRef = this.dialog.open(AssignUmpiresDialogComponent, config);
    dialogRef.afterClosed().subscribe(result => {
      if (result.action === 'assign') {
        console.log('got umpire work', result.umpireWork);
      }
    });

  }
}
