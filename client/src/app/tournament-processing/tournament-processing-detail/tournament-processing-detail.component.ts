import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChange, SimpleChanges} from '@angular/core';
import {TournamentProcessingRequest} from '../model/tournament-processing-request';
import {TournamentProcessingRequestDetail} from '../model/tournament-processing-request-detail';
import {MatDialog} from '@angular/material/dialog';
import {GenerateReportsDialogComponent} from '../generate-reports-dialog/generate-reports-dialog.component';
import {DateUtils} from '../../shared/date-utils';

@Component({
  selector: 'app-tournament-processing-detail',
  templateUrl: './tournament-processing-detail.component.html',
  styleUrls: ['./tournament-processing-detail.component.scss']
})
export class TournamentProcessingDetailComponent implements OnInit, OnChanges {

  @Input()
  public tournamentProcessingRequest: TournamentProcessingRequest;

  @Input()
  public generatingReports: boolean;

  @Output('generateReports')
  public generateReportsEventEmitter: EventEmitter<any> = new EventEmitter<any>();

  constructor(private dialog: MatDialog) { }

  ngOnInit(): void {
  }

  ngOnChanges(changes: SimpleChanges): void {
    const tprChanges: SimpleChange = changes.tournamentProcessingRequest;
    if (tprChanges != null) {
      const tpr: TournamentProcessingRequest = tprChanges.currentValue;
      if (tpr) {
        const details = tpr.details;
        if (details != null && details.length > 0) {
          // sort requests so the newest are at the top
          const dateUtils = new DateUtils();
          details.sort((detail1: TournamentProcessingRequestDetail, detail2: TournamentProcessingRequestDetail) => {
            if (detail1.createdOn == null) {
              return -1;
            } else if (detail2.createdOn == null) {
              return 1;
            } else {
              return dateUtils.isDateBefore(detail1.createdOn, detail1.createdOn) ? 1 : -1;
            }
          });
          tpr.details = JSON.parse(JSON.stringify(details));
        }
      }
    }
  }

  generateAllReports() {
    const config = {
      width: '500px', height: '450px'
    };
    const dialogRef = this.dialog.open(GenerateReportsDialogComponent, config);
    dialogRef.afterClosed().subscribe(result => {
      if (result.action === 'ok') {
        const request = JSON.parse(JSON.stringify(this.tournamentProcessingRequest));
        const details = request.details || [];
        details.push(new TournamentProcessingRequestDetail());
        request.details = details;
        request.remarks = result.remarks;
        request.ccLast4Digits = result.ccLast4Digits;
        this.generateReportsEventEmitter.emit(request);
      }
    });


  }

  onSubmitRequest() {

  }

}
