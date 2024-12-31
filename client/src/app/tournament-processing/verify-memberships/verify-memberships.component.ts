import {Component, Input} from '@angular/core';
import {MembershipInfo} from '../model/membership-info.model';
import {MembershipUtil} from '../../tournament/util/membership-util';
import {MembershipType} from '../../tournament/tournament-entry/model/tournament-entry.model';
import {DateUtils} from '../../shared/date-utils';

@Component({
  selector: 'app-verify-memberships',
  templateUrl: './verify-memberships.component.html',
  styleUrl: './verify-memberships.component.scss'
})
export class VerifyMembershipsComponent {
  // url to return to after viewing this page
  @Input()
  returnUrl: string;

  // url to return to after viewing profile
  @Input()
  thisUrl: string | null;

  @Input()
  tournamentName: string;

  @Input()
  tournamentId!: number;

  @Input()
  tournamentStartDate: Date | null;

  @Input()
  membershipInfos!: MembershipInfo[] | null;

  @Input() countPassAdult: number;
  @Input() countPassJunior: number;
  @Input() countBasic!: number;
  @Input() countPro!: number;
  @Input() countLifetime!: number;

  membershipOptions: any [];
  dateUtils: DateUtils = new DateUtils();

  protected readonly MembershipType = MembershipType;


  constructor() {
    this.membershipOptions = new MembershipUtil().getMembershipOptions();
  }

  getMembershipOptionLabel(membershipType: MembershipType) {
    for (const membershipOption of this.membershipOptions) {
      if (membershipOption.value === membershipType.valueOf()) {
        return membershipOption.label;
      }
    }
    return 'Unknown Membership Type'; //membershipType.valueOf();
  }

  isUnpaid(expirationDate: Date, membershipType: MembershipType) {
    if (membershipType === MembershipType.NO_MEMBERSHIP_REQUIRED) {
      if (expirationDate != null && this.tournamentStartDate != null) {
        return this.dateUtils.isDateBefore(expirationDate, this.tournamentStartDate)
      } else {
        return false;
      }
    } else {
      return false;
    }
  }
}
