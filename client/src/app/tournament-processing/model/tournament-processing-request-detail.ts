import {TournamentProcessingRequestStatus} from './tournament-processing-request-status';

/**
 * Each time tournament director generates and submits reports this will be created
 * If report needs to be updated and resubmitted we create another detail like this.
 */
export class TournamentProcessingRequestDetail {

  // id of this data
  id: number;

  // date of creation
  createdOn: Date;

  // profile id of the person who created this request
  createdByProfileId: string;

  // status of this processing request
  status: TournamentProcessingRequestStatus = TournamentProcessingRequestStatus.New;

  // paths in repository where reports are stored
  pathTournamentReport: string;
  pathPlayerList: string;
  pathApplications: string;
  pathMembershipList: string;
  pathMatchResults: string;

  // id of the payment used to pay for this request (i.e. tournament report)
  paymentId: number;

  // amount to be paid if any
  amountToPay: number;

  // date of payment
  paidOn: Date;

  constructor() {
    this.paymentId = 0;
    this.amountToPay = 0;
  }
}
