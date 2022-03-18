import {TournamentProcessingRequestDetail} from './tournament-processing-request-detail';
import {TournamentProcessingRequestStatus} from './tournament-processing-request-status';

export class TournamentProcessingRequest {

  // id of this data
  id: number;

  // if of the tournament
  tournamentId: number;

  // name of tournament - to avoid having to go into db to get the names
  tournamentName: string;

  // status of this processing request
  status: TournamentProcessingRequestStatus = TournamentProcessingRequestStatus.New;

  // list of details with each report, in case reports are resubmitted
  details: TournamentProcessingRequestDetail[] = [];
}
