export class Profile {
  userId: string;
  firstName: string;
  lastName: string;
  mobilePhone: string;
  email: string;
  city: string;
  state: string;
  zipCode: string;

  constructor() {

  }

  fromFormValues(formValues: any) {
    this.userId = formValues.userId;
    this.firstName = formValues.firstName;
    this.lastName = formValues.lastName;
    this.mobilePhone = formValues.mobilePhone;
    this.email = formValues.email;
    this.city = formValues.city;
    this.state = formValues.state;
    this.zipCode = formValues.zipCode;
  }
}
