import {InsuranceRequestStatus} from './insurance-request-status';

export enum AdditionalInsuredRole {
  None = 'None',
  OwnerOfPremises = 'OwnerOfPremises',
  Sponsor = 'Sponsor',
  Other = 'Other'
}

export class InsuranceRequest {
  id: number;
  orgName: string;
  orgStreetAddress: string;
  orgCity: string;
  orgZip: number;
  orgState: string;

  requestDate: Date;

  contactName: string;
  contactPhoneNumber: string;
  contactEmail: string;

  certFacilityName: string;
  certPersonName: string;
  certPersonPhoneNumber: string;
  certPersonEmail: string;

  certStreetAddress: string;
  certCity: string;
  certState: string;
  certZip: number;

  eventName: string;
  eventStartDate: Date;
  eventEndDate: Date;

  isAdditionalInsured: boolean;
  additionalInsuredName: string;

  additionalInsuredRole = AdditionalInsuredRole.None;
  otherRoleDescription: string;

  status: InsuranceRequestStatus = InsuranceRequestStatus.New;

  constructor() {

  }
}
