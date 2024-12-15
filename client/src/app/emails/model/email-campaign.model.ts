export class EmailCampaign {

  // unique id of a campaign
  id: number;

  // name of campaign e.g. 2024 Aurora Summer Open announcement
  name: string;

  // subject of the email
  subject: string;

  // text of the email body
  body: string;

  // date sent
  dateSent: Date;

  // name of the tournament for which it was sent out
  tournamentName: string;

  // number of emails sent
  emailsCount: number;

  // filters to apply to recipients - by 0 - all or event id.
  recipientFilters: number [];

  // recipients to remove from the list
  removedRecipients: Recipient [];

  // if true ignore recipient filter and get all recipients in the database
  allRecipients: boolean;

  // if true exclude those from the list who are already registered for this tournament
  excludeRegistered: boolean;

  // state abbreviations to filter by
  stateFilters: string [];

  // treat body of this email as html when sending
  htmlEmail: boolean;
}

/**
 * Email recipient which should be removed after filtering
 */
export class Recipient {
  firstName: string;

  lastName: string;

  emailAddress: string;

  public getFullName() {
    return `${this.lastName}, ${this.firstName}`
  }
}
