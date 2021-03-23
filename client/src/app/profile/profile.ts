import {DateUtils} from '../shared/date-utils';

export class Profile {
  userId: string;
  login: string;
  membershipId: number;
  firstName: string;
  lastName: string;
  mobilePhone: string;
  email: string;
  streetAddress: string;
  city: string;
  state: string;
  zipCode: string;
  countryCode: string;
  gender: string;
  dateOfBirth: Date;
  membershipExpirationDate: Date;
  tournamentRating: number;

  constructor() {

  }

  fromFormValues(formValues: any) {
    this.userId = formValues.userId;
    this.login = formValues.login;
    this.firstName = formValues.firstName;
    this.lastName = formValues.lastName;
    this.mobilePhone = formValues.mobilePhone;
    this.email = formValues.email;
    this.streetAddress = formValues.streetAddress;
    this.city = formValues.city;
    this.state = formValues.state;
    this.zipCode = formValues.zipCode;
    this.countryCode = formValues.countryCode;
    this.gender = formValues.gender;
    this.dateOfBirth = formValues.dateOfBirth;
    this.membershipId = formValues.membershipId;
    this.membershipExpirationDate = new DateUtils().convertFromString(formValues.membershipExpirationDate);
    this.tournamentRating = formValues.tournamentRating;
  }
}
