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
  homeClubId: number;
  homeClubName: string;
  division: string;

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
    this.homeClubId = formValues.homeClubId;
    this.homeClubName = formValues.homeClubName;
    this.division = formValues.division;
  }

  clone(otherProfile: Profile) {
    this.userId = otherProfile.userId;
    this.login = otherProfile.login;
    this.firstName = otherProfile.firstName;
    this.lastName = otherProfile.lastName;
    this.mobilePhone = otherProfile.mobilePhone;
    this.email = otherProfile.email;
    this.streetAddress = otherProfile.streetAddress;
    this.city = otherProfile.city;
    this.state = otherProfile.state;
    this.zipCode = otherProfile.zipCode;
    this.countryCode = otherProfile.countryCode;
    this.gender = otherProfile.gender;
    this.dateOfBirth = otherProfile.dateOfBirth;
    this.membershipId = otherProfile.membershipId;
    this.membershipExpirationDate = otherProfile.membershipExpirationDate;
    this.tournamentRating = otherProfile.tournamentRating;
    this.homeClubId = otherProfile.homeClubId;
    this.homeClubName = otherProfile.homeClubName;
    this.division = otherProfile.division;
  }
}
