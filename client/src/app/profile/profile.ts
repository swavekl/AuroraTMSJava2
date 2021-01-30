export class Profile {
  userId: string;
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

  constructor() {

  }

  fromFormValues(formValues: any) {
    this.userId = formValues.userId;
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
  }
}
