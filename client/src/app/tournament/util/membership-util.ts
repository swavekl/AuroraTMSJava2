import {MembershipType} from '../tournament-entry/model/tournament-entry.model';
import {DateUtils} from '../../shared/date-utils';

/**
 * USATT Membership options utility
 */
export class MembershipUtil {

  private membershipOptions: any [] = [
    {value: MembershipType.NO_MEMBERSHIP_REQUIRED.valueOf(), label: 'My Membership is up to date', cost: 0, available: true},
    {value: MembershipType.TOURNAMENT_PASS.valueOf(), label: 'One Tournament Pass (0 – 4 star)', cost: 20, available: true},
    {value: MembershipType.BRONZE.valueOf(), label: 'Bronze Tier 1 year', cost: 25, available: true},
    {value: MembershipType.SILVER.valueOf(), label: 'Silver Tier 1 year (0 – 4 star)', cost: 50, available: true},
    {value: MembershipType.GOLD.valueOf(), label: 'Gold Tier 1 year (0 - 5 star)', cost: 100, available: true},
    {value: MembershipType.LIFETIME.valueOf(), label: 'Lifetime', cost: 1300, available: true}
  ];

  public getMembershipOptions(): any [] {
    return this.membershipOptions;
  }

  /**
   *
   * @param dateOfBirth
   * @param tournamentStartDate
   * @param tournamentStarLevel
   */
  public hideMembershipOptions(dateOfBirth: Date, tournamentStartDate: Date, tournamentStarLevel: number) {
    const isJunior = this.isPlayerAJunior(dateOfBirth, tournamentStartDate);
    this.membershipOptions.forEach((membershipOption: any) => {
      switch (membershipOption.value) {
        case MembershipType.TOURNAMENT_PASS_JUNIOR:
          membershipOption.available = isJunior && (tournamentStarLevel === 5);
          break;
        case MembershipType.TOURNAMENT_PASS_ADULT:
          membershipOption.available = !isJunior && (tournamentStarLevel === 5);
          break;
        case MembershipType.TOURNAMENT_PASS:
        case MembershipType.SILVER:
          membershipOption.available = (tournamentStarLevel >= 0 && tournamentStarLevel <= 4);
          break;
      }
    });
  }

  /**
   *
   * @param dateOfBirth
   * @param tournamentStartDate
   * @param membershipExpirationDate
   * @param tournamentStarLevel
   */
  getInitialMembershipOption(dateOfBirth: Date, membershipExpirationDate: Date, tournamentStartDate: Date, tournamentStarLevel: number): MembershipType {
    let membershipType: MembershipType = MembershipType.NO_MEMBERSHIP_REQUIRED;
    let membershipExpired = true;
    // foreign association players don't have expiration date internally
    // must provide proof of non-expired membership from their association
    if (membershipExpirationDate != null) {
      membershipExpired = new DateUtils().isDateBefore(membershipExpirationDate, tournamentStartDate);
    }
    if (membershipExpired) {
        membershipType =  ((tournamentStarLevel >= 0 && tournamentStarLevel <= 4)) ?
          MembershipType.SILVER : MembershipType.GOLD;
    }
    return membershipType;
  }

  /**
   * Checks if a player will be junior on the tournament start date
   * @param dateOfBirth
   * @param tournamentStartDate
   */
  public isPlayerAJunior(dateOfBirth: Date, tournamentStartDate: Date) {
    if (dateOfBirth != null) {
      const ageOnTournamentStartDate = new DateUtils().getAgeOnDate(dateOfBirth, tournamentStartDate);
      return ageOnTournamentStartDate < 18;
    } else {
      return false;
    }
  }

  /**
   * Validates if a player's membership tier satisfies the tournament requirements.
   * * @param starLevel The star rating of the tournament (0 - 5).
   * @param starLevel tournament star level
   * @param playerMembershipLevel The player's actual active membership tier.
   * @returns boolean true if the player is eligible to play; false otherwise.
   */
  public isMembershipValid(
    starLevel: number,
    playerMembershipLevel: MembershipType
  ): boolean {

    // 2. 5-Star tournaments strictly require GOLD or LIFETIME
    if (starLevel === 5) {
      return playerMembershipLevel === MembershipType.GOLD || playerMembershipLevel === MembershipType.LIFETIME;
    }

    if (starLevel >= 0 && starLevel <= 4) {
      return playerMembershipLevel === MembershipType.SILVER || playerMembershipLevel === MembershipType.GOLD || playerMembershipLevel === MembershipType.LIFETIME;
    }

    return false;
  }

  /**
   * Determines the minimum required membership level based on the tournament's star rating.
   *
   * @param starLevel The star rating of the tournament (0 - 5).
   * @returns The required MembershipType (SILVER or GOLD).
   */
  public getRequiredMembershipLevel(starLevel: number): MembershipType {
    let membershipType: MembershipType;

    if (starLevel === 5) {
      membershipType = MembershipType.GOLD;
    } else if (starLevel >= 0 && starLevel <= 4) {
      membershipType = MembershipType.SILVER;
    }

    return membershipType;
  }
}
