import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChange, SimpleChanges} from '@angular/core';
import {TournamentProcessingRequest} from '../model/tournament-processing-request';
import {TournamentProcessingRequestDetail} from '../model/tournament-processing-request-detail';
import {MatDialog} from '@angular/material/dialog';
import {GenerateReportsDialogComponent} from '../generate-reports-dialog/generate-reports-dialog.component';
import {DateUtils} from '../../shared/date-utils';
import {TournamentProcessingRequestStatus} from '../model/tournament-processing-request-status';
import {AuthenticationService} from '../../user/authentication.service';
import {UserRoles} from '../../user/user-roles.enum';
import {FileRepositoryService} from '../../shared/upload-button/file-repository.service';
import {ConfirmationPopupComponent} from '../../shared/confirmation-popup/confirmation-popup.component';
import {Router} from '@angular/router';

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

  @Output('requestEvent')
  public requestEventEmitter: EventEmitter<any> = new EventEmitter<any>();

  // currency in which to pay the association e.g. USATT
  @Input()
  currencyCode: string;

  @Input()
  returnUrl: string;

  canGenerateReports: boolean;

  canProcessReports: boolean;

  constructor(private dialog: MatDialog,
              private authenticationService: AuthenticationService,
              private fileRepositoryService: FileRepositoryService,
              private router: Router) {
  }

  ngOnInit(): void {
    const roles: string [] = [UserRoles.ROLE_TOURNAMENT_DIRECTORS.valueOf()];
    this.canGenerateReports = this.authenticationService.hasCurrentUserRole(roles);
    const processorRoles: string [] = [UserRoles.ROLE_USATT_TOURNAMENT_MANAGERS.valueOf()];
    this.canProcessReports = this.authenticationService.hasCurrentUserRole(processorRoles);
  }

  isGenerateReportsEnabled(): boolean {
    return this.canGenerateReports && !this.generatingReports;
  }


  onDeleteRequest(detailId: number) {
    const config = {
      width: '450px', height: '230px', data: {
        message: `Are you sure you want to delete these reports?`,
      }
    };
    const dialogRef = this.dialog.open(ConfirmationPopupComponent, config);
    dialogRef.afterClosed().subscribe(result => {
      if (result === 'ok') {
        const request = JSON.parse(JSON.stringify(this.tournamentProcessingRequest));
        let details: TournamentProcessingRequestDetail [] = request.details || [];
        request.details = details.filter((detail: TournamentProcessingRequestDetail) => {
          return (detail.id != detailId);
        });
        const event = {action: 'delete', request: request, detailId: detailId};
        this.requestEventEmitter.emit(event);
      }
    });


  }

  isDeleteReportsEnabled(detail: TournamentProcessingRequestDetail): boolean {
    return this.canGenerateReports && (detail?.status === TournamentProcessingRequestStatus.New && detail?.createdOn != null);
  }

  isSubmitReportsEnabled(detail: TournamentProcessingRequestDetail): boolean {
    return this.canGenerateReports && (detail?.status === TournamentProcessingRequestStatus.New && detail?.createdOn != null);
  }

  isPaymentButtonEnabled(detail: TournamentProcessingRequestDetail): boolean {
    return (detail.status === TournamentProcessingRequestStatus.Submitted &&
      detail.amountToPay > 0 && this.canProcessReports);
  }

  isMarkProcessedEnabled(detail: TournamentProcessingRequestDetail) {
    return this.canProcessReports &&
      (detail.status === TournamentProcessingRequestStatus.Paid ||
        detail.status === TournamentProcessingRequestStatus.Submitted);
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
              return 1;
            } else if (detail2.createdOn == null) {
              return -1;
            } else {
              return dateUtils.isTimestampBefore(detail1.createdOn, detail2.createdOn) ? -1 : 1;
            }
          });
          tpr.details = JSON.parse(JSON.stringify(details));
        }
      }
    }
  }

  generateAllReports() {
    const config = {
      width: '700px', height: '570px'
    };
    const dialogRef = this.dialog.open(GenerateReportsDialogComponent, config);
    dialogRef.afterClosed().subscribe(result => {
      if (result.action === 'ok') {
        const request = JSON.parse(JSON.stringify(this.tournamentProcessingRequest));
        const details = request.details || [];
        const detail = new TournamentProcessingRequestDetail();
        detail.generateTournamentReport = result.generateTournamentReport ;
        detail.generateApplications = result.generateApplications;
        detail.generatePlayerList = result.generatePlayerList ;
        detail.generateMatchResults = result.generateMatchResults ;
        detail.generateMembershipList = result.generateMembershipList;
        detail.generateDeclarationOfCompliance = result.generateDeclarationOfCompliance;
        detail.generateRankingReport = result.generateRankingReport;
        detail.rankingReportTournamentId = result.rankingReportTournamentId;
        details.push(detail);
        request.details = details;
        request.remarks = result.remarks;
        request.ccLast4Digits = result.ccLast4Digits;
        this.requestEventEmitter.emit({action: 'generate', request: request, detailId: detail.id});
      }
    });
  }

  onSubmitRequest(detailId: number) {
    const request = JSON.parse(JSON.stringify(this.tournamentProcessingRequest));
    const details = request.details || [];
    for (let i = 0; i < details.length; i++) {
      const detail = details[i];
      if (detail.id === detailId) {
        detail.status = TournamentProcessingRequestStatus.Submitting;
        this.requestEventEmitter.emit({action: 'submit', request: request, detailId: detailId});
        break;
      }
    }
  }

  onPay(detailId: number) {
    const request = JSON.parse(JSON.stringify(this.tournamentProcessingRequest));
    const event = {action: 'pay', request: request, detailId: detailId};
    this.requestEventEmitter.emit(event);
  }

  onProcess(detailId: number) {
    const request = JSON.parse(JSON.stringify(this.tournamentProcessingRequest));
    const details = request.details || [];
    for (let i = 0; i < details.length; i++) {
      const detail = details[i];
      if (detail.id === detailId) {
        detail.status = TournamentProcessingRequestStatus.Processed;
        this.requestEventEmitter.emit({action: 'process', request: request, detailId: detailId});
        break;
      }
    }
  }

  onDownloadDFile(fileUrl: string): boolean {
    this.fileRepositoryService.download(fileUrl);
    return false;
  }

  verifyMemberships() {
    const url = `/ui/processing/verifymemberships/${this.tournamentProcessingRequest.tournamentId}/${this.tournamentProcessingRequest.tournamentName}`;
    const returnURL = `/ui/processing/detail/submit/${this.tournamentProcessingRequest.tournamentId}/${this.tournamentProcessingRequest.tournamentName}`;
    const extras = {state: {returnUrl: returnURL }};
    this.router.navigateByUrl(url, extras);
  }
}
