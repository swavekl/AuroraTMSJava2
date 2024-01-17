export class EmailCampaign {

  // unique id of a campaign
  id: number;

  // name of campaign e.g. 2024 Aurora Summer Open announcement
  name: string;

  // subject of the email
  subject: string;

  // text of the email body
  body: string;

  // filters to apply to recipients - by 0 - all or event id.
  recipientFilters: number [];

  // recipients to remove from the list
  removedRecipients: Recipient [];
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
