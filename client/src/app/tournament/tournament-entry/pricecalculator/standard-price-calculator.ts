import {PriceCalculator} from './price-calculator';
import {AbstractPriceCalculator} from './abstract-price-calculator';
import {EventEntryStatus} from '../model/event-entry-status.enum';
import {MembershipType} from '../model/tournament-entry.model';
import {EventEntryType} from '../../tournament-config/model/event-entry-type.enum';
import {FeeStructure} from '../../tournament-config/model/fee-structure.enum';
import {DateUtils} from '../../../shared/date-utils';
import {FeeScheduleItem} from '../../tournament-config/model/fee-schedule-item';
import {Team} from '../model/team.model';
import {TournamentEventEntryInfo} from '../model/tournament-event-entry-info-model';

export class StandardPriceCalculator extends AbstractPriceCalculator implements PriceCalculator {

  getTotalPrice(membershipOption: MembershipType,
                usattDonation: number,
                enteredEvents: TournamentEventEntryInfo[],
                teams: Team[],
                isWithdrawing: boolean,
                availableEvents: TournamentEventEntryInfo [] = []): number {
    let total = 0;
    this.initiateReport();

    const isEntered = this.isEnteredInAnyEvents(enteredEvents);
    for (let i = 0; i < this.membershipOptions.length; i++) {
      const option = this.membershipOptions[i];
      if (option.value === membershipOption) {
        if (isEntered) {
          total += option.cost;
          this.addMembershipOptionLine(option);
        }
        break;
      }
    }
    this.addEventsHeader();
    // add for those events that were entered in this session and subtract for those that were dropped
    for (let i = 0; i < enteredEvents.length; i++) {
      const enteredEvent = enteredEvents[i];
      if (enteredEvent.status === EventEntryStatus.PENDING_CONFIRMATION ||
        enteredEvent.status === EventEntryStatus.ENTERED) {
        total += enteredEvent.price;
        this.addEvent(enteredEvent);
      }
    }

    // check if there are any waiting list entries so we can skip it if necessary
    let hasWaitedEvents: boolean = false;
    for (let i = 0; i < enteredEvents.length; i++) {
      const enteredEvent = enteredEvents[i];
      if (enteredEvent.status === EventEntryStatus.ENTERED_WAITING_LIST ||
        enteredEvent.status === EventEntryStatus.PENDING_WAITING_LIST) {
        hasWaitedEvents = true;
        break;
      }
    }

    if (hasWaitedEvents) {
      // waited events
      this.addWaitedEventsHeader();
      for (let i = 0; i < enteredEvents.length; i++) {
        const enteredEvent = enteredEvents[i];
        if (enteredEvent.status === EventEntryStatus.ENTERED_WAITING_LIST ||
          enteredEvent.status === EventEntryStatus.PENDING_WAITING_LIST) {
          this.addWaitedEvent(enteredEvent);
        }
      }
    }

    // various fees
    this.addFeesSection();

    total += usattDonation;
    this.addUsattDonation(usattDonation);

    if (total > 0) {
      // add registration fee
      if (this.registrationFee > 0) {
        total += this.registrationFee;
        this.addRegistrationFeeLine();
      }

      total += this.addTeamEventPerPlayerFees(enteredEvents, teams);

      if (this.isLateEntry && this.lateEntryFee > 0) {
        total += this.lateEntryFee;
        this.addLateEntryFeeLine();
      }

    }

    if (isWithdrawing) {
      const penaltyFee = this.addWithdrawalPenaltyFee(availableEvents);
      total += penaltyFee;
    }
    console.log('total', total);
    return total;
  }

  private addTeamEventPerPlayerFees(enteredEvents: TournamentEventEntryInfo[], teams: Team []): number {
    let totalPerPlayer = 0;
    // if entered team tournament
    for (let i = 0; i < enteredEvents.length; i++) {
      const enteredEvent = enteredEvents[i];
      if (enteredEvent.status === EventEntryStatus.PENDING_CONFIRMATION ||
        enteredEvent.status === EventEntryStatus.ENTERED) {
        if (enteredEvent.event != null) {
          if (enteredEvent.event.eventEntryType === EventEntryType.TEAM) {
            // find number of players in a team
            if (teams != null && teams.length > 0) {
              for (const team of teams) {
                if (team.tournamentEventFk == enteredEvent.eventFk) {
                  let numTeamPlayers = (team.teamMembers != null) ? team.teamMembers.length : 0;
                  const teamEventTotalPerPlayer  = enteredEvent.event.perPlayerFee * numTeamPlayers;
                  totalPerPlayer += teamEventTotalPerPlayer;
                  this.addPerPlayerFeeLine(teamEventTotalPerPlayer, enteredEvent.event.name);
                }
              }
            }
          }
        }
      }
    }
    return totalPerPlayer;
  }

  private addWithdrawalPenaltyFee(availableEvents: any[]) {
    let totalPenaltyFee = 0;
    const today = new Date();
    // if entered team tournament
    for (let i = 0; i < availableEvents.length; i++) {
      const availableEvent = availableEvents[i];
      if (availableEvent.status === EventEntryStatus.PENDING_DELETION) {
        if (availableEvent.event != null) {
          if (availableEvent.event.eventEntryType === EventEntryType.TEAM) {
            if (availableEvent.event.feeStructure == FeeStructure.FIXED) {

            } else if (availableEvent.event.feeStructure == FeeStructure.PER_SCHEDULE) {
              const fsItems: FeeScheduleItem [] = availableEvent.event.configuration.feeScheduleItems || [];
              const dateUtils = new DateUtils();
              const mutableFsItems: FeeScheduleItem [] = [...fsItems]; // make a mutable copy for sorting
              mutableFsItems.sort((fsi1: FeeScheduleItem, fsi2: FeeScheduleItem) => {
                return dateUtils.isDateBefore(fsi1.deadline, fsi2.deadline) ? -1 : 1;
              });
              for (const fsItem of mutableFsItems) {
                if (dateUtils.isDateSameOrBefore(today, fsItem.deadline)) {
                  this.addCancellationFeeLine(fsItem.cancellationFee, availableEvent.event.name);
                  totalPenaltyFee +=  fsItem.cancellationFee;
                  break;
                }
              }
            }
          }
        }
      }
    }
    return totalPenaltyFee;
  }
}
