import {RegistrationEventType} from './registration-event-type.enum';

export class Registration {

  // tournament entry id, or clinic or seminar etc.
  id: number;

  // tournament id, or clinic id etc.
  activityId: number;

  // name of the event e.g. tournament or clinic name
  name: string;

  // registration type
  registrationEventType: RegistrationEventType;

  // start and end date
  startDate: Date;
  endDate: Date;

  // total paid for registration
  cost: number;

  // event specific information like number of entered events
  info: string;
}
