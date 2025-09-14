import {Component, EventEmitter, Input, Output} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {MembershipInfo} from '../model/membership-info.model';
import {MembershipUtil} from '../../tournament/util/membership-util';
import {MembershipType} from '../../tournament/tournament-entry/model/tournament-entry.model';
import {DateUtils} from '../../shared/date-utils';
import {ConfirmationPopupComponent} from '../../shared/confirmation-popup/confirmation-popup.component';

@Component({
    selector: 'app-verify-memberships',
    templateUrl: './verify-memberships.component.html',
    styleUrl: './verify-memberships.component.scss',
    standalone: false
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

  @Output()
  contactPlayers: EventEmitter<MembershipInfo[]> = new EventEmitter<MembershipInfo[]>;

  membershipOptions: any [];
  dateUtils: DateUtils = new DateUtils();

  protected readonly MembershipType = MembershipType;


  constructor(private dialog: MatDialog) {
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

  /**
   * Checks if marked as current but not paid
   * @param expirationDate
   * @param membershipType
   */
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

  /**
   * Checks if membership was paid before tournament and will not be necessary at the tournament
   * @param expirationDate
   * @param membershipType
   */
  isPaidUnnecessarily(expirationDate: Date, membershipType: MembershipType): boolean {
    if (membershipType !== MembershipType.NO_MEMBERSHIP_REQUIRED) {
      if (expirationDate != null && this.tournamentStartDate != null) {
        return this.dateUtils.isDateBefore(this.tournamentStartDate, expirationDate)
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  onContactPlayers() {
    const playersNeedingContact: MembershipInfo[] = this.membershipInfos.filter(
      (membershipInfo: MembershipInfo): boolean => {
        return this.isUnpaid(membershipInfo.expirationDate, membershipInfo.membershipType) ||
          this.isPaidUnnecessarily(membershipInfo.expirationDate, membershipInfo.membershipType);
      });

    if (playersNeedingContact.length > 0) {
      const config = {
        width: '450px', height: '220px', data: {
          contentAreaHeight: '150px',
          message: `'There are ${playersNeedingContact.length} players who need to pay or be refunded for their USATT memberships.
          Do you want to send emails to them to alert them ?`,
        }
      };
      const dialogRef = this.dialog.open(ConfirmationPopupComponent, config);
      dialogRef.afterClosed().subscribe(result => {
        if (result === 'ok') {
          this.contactPlayers.emit(playersNeedingContact);
        }
      });
    }
  }

  getUnpaidCount() {
    const unpaidMemberships: MembershipInfo[] = this.membershipInfos.filter(
      (membershipInfo: MembershipInfo): boolean => {
        return this.isUnpaid(membershipInfo.expirationDate, membershipInfo.membershipType);
      });
    return unpaidMemberships.length;
  }

  getUnnecessaryPaidCount() {
    const unnecessarilyPaidMemberships: MembershipInfo[] = this.membershipInfos.filter(
      (membershipInfo: MembershipInfo): boolean => {
        return this.isPaidUnnecessarily(membershipInfo.expirationDate, membershipInfo.membershipType);
      });
    return unnecessarilyPaidMemberships.length;
  }
}
