import {TournamentProcessingRequestDetail} from './tournament-processing-request-detail';
import {TournamentProcessingRequestStatus} from './tournament-processing-request-status';

export class TournamentProcessingRequest {

  // id of this data
  id: number;

  // if of the tournament
  tournamentId: number;

  // name of tournament - to avoid having to go into db to get the names
  tournamentName: string;

  // status of this processing request - derived from the status of last detail
  requestStatus: TournamentProcessingRequestStatus = TournamentProcessingRequestStatus.New;

  // list of details with each report, in case reports are resubmitted
  details: TournamentProcessingRequestDetail[] = [];

  // remarks to be placed on the tournament report
  remarks: string;

  // last 4 digits of a credit card to be placed on tournament report
  ccLast4Digits: string;

  public getLatestStatus(): TournamentProcessingRequestStatus {
    let latestStatus: TournamentProcessingRequestStatus = TournamentProcessingRequestStatus.New;
    for (let i = 0; i < this.details.length; i++) {
      const detail = this.details[i];
      latestStatus = (detail.status < latestStatus) ? detail.status : latestStatus;
    }
    return latestStatus;
  }
}
