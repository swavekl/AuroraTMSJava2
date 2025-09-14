import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'phone',
    standalone: false
})
export class PhonePipe implements PipeTransform {

  transform(phoneNumber: string, formatAsUrl: boolean, ...args: unknown[]): unknown {
    const regex: RegExp = /(\d{3})[\s-]?(\d{3})[\s-]?(\d{4})/
    let formattedPhoneNumber: string = phoneNumber;
    if (phoneNumber != null && regex.test(phoneNumber)) {
      const regExpMatchArray: RegExpMatchArray = phoneNumber.match(regex);
      if (regExpMatchArray != null) {
        formattedPhoneNumber =
          `${regExpMatchArray[1]} ${regExpMatchArray[2]} ${regExpMatchArray[3]}`;
      }
    }
    if (formattedPhoneNumber != null && formatAsUrl === true) {
      formattedPhoneNumber = 'tel:' + formattedPhoneNumber;
    }
    return formattedPhoneNumber;
  }

}
