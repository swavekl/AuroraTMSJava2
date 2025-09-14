import {AfterViewInit, Component, Input, ViewChild} from '@angular/core';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {AssignUmpiresDialogComponent} from '../assign-umpires-dialog/assign-umpires-dialog.component';
import {Personnel} from '../../tournament/tournament-config/model/personnel.model';
import {UmpiringService} from '../service/umpiring.service';
import {first, map, tap} from 'rxjs/operators';
import {UmpireSummaryTableComponent} from './umpire-summary-table/umpire-summary-table.component';
import {UmpiredMatchInfo} from '../model/umpired-match-info.model';

@Component({
    selector: 'app-umpire-management',
    templateUrl: './umpire-management.component.html',
    styleUrl: './umpire-management.component.scss',
    standalone: false
})
export class UmpireManagementComponent implements AfterViewInit {

  @Input()
  tournamentId: number;

  @Input()
  tournamentDay!: number;

  @Input()
  tournamentName: string;

  @Input()
  umpireList!: Personnel[];

  @Input()
  refereeList!: Personnel[];

  @ViewChild(UmpireSummaryTableComponent)
  umpireSummaryTableComponent: UmpireSummaryTableComponent;
  umpireMatchInfos: UmpiredMatchInfo[] = [];
  selectedUmpireName: string;

  constructor(private umpiringService: UmpiringService,
              private dialog: MatDialog) {
  }

  ngAfterViewInit(): void {
    if (this.umpireSummaryTableComponent != null) {
      this.umpireSummaryTableComponent.refresh();
    }
  }

  onViewUmpireDetails(umpireProfileId: string) {
    // record selected umpire name
    if (umpireProfileId != null) {
      const selectedUmpires: Personnel[] = this.umpireList.filter((personnel: Personnel) => {
        return personnel.profileId === umpireProfileId;
      });
      this.selectedUmpireName = (selectedUmpires?.length > 0) ? selectedUmpires[0].name : '';

      this.umpiringService.getUmpireMatches(umpireProfileId, this.tournamentId)
        .pipe(
          first(),
          map((umpiredMatchInfos: UmpiredMatchInfo[]) => {
            this.umpireMatchInfos = umpiredMatchInfos;
          })
        )
        .subscribe();
    } else {
      this.umpireMatchInfos = [];
    }
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
        this.umpiringService.assign(result.umpireWork)
          .pipe(
            first(),
            tap(() => {
              this.umpireSummaryTableComponent.refresh();
            })
          ).subscribe();
      }
    });
  }

  public getRefereeNames(): string {
    let refereeNames = '';
    if (this.refereeList != null) {
      for (const personnel of this.refereeList) {
        refereeNames += (refereeNames.length === 0) ? '' : ', ';
        refereeNames += personnel.name;
      }
    }
    return refereeNames;
  }
}
