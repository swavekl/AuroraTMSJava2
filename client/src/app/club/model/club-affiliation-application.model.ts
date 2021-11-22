/**
 * Club affiliation application
 */
import {DateUtils} from '../../shared/date-utils';
import {ClubAffiliationApplicationStatus} from './club-affiliation-application-status';
import {PlayingSite} from './playing-site';

export class ClubAffiliationApplication {
  id: number;
  name: string;
  buildingName: string;
  streetAddress: string;
  city: string;
  state: string;
  zipCode: number;
  status: ClubAffiliationApplicationStatus = ClubAffiliationApplicationStatus.New;
  affiliationExpirationDate: Date;
  // payment id if the application fee of $75 was paid
  paymentId: number;

  // Wednesday & Friday - 6:30 - 9:30PM
  hoursAndDates: string;

  mailingCorrespondentsName: string;
  mailingStreetAddress: string;
  mailingCity: string;
  mailingState: string;
  mailingZipCode: string;

  clubAdminName: string;
  clubAdminEmail: string;
  clubPhoneNumber: string;
  clubPhoneNumber2: string;
  clubWebsite: string;

  presidentName: string;
  presidentEmail: string;
  presidentPhoneNumber: string;

  vicePresidentName: string;
  vicePresidentEmail: string;
  vicePresidentPhoneNumber: string;

  secretaryName: string;
  secretaryEmail: string;
  secretaryPhoneNumber: string;

  treasurerName: string;
  treasurerEmail: string;
  treasurerPhoneNumber: string;

  hasMembershipStructure: boolean;
  membershipStructure: string;

  membersCount: number;
  tablesCount: number;

  programs: string;
  hasBankAccount: boolean;

  alternatePlayingSites: PlayingSite[];

  constructor() {

  }

  applyChanges(formValues: any) {
    const dateUtils = new DateUtils();
    formValues.affiliationExpirationDate = dateUtils.convertFromLocalToUTCDate(formValues.affiliationExpirationDate);
    Object.assign(this, formValues);

  }

  deepClone(): ClubAffiliationApplication {
    return JSON.parse(JSON.stringify(this));
  }
}
