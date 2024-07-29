import {DateUtils} from '../../shared/date-utils';
import {TournamentEvent} from './tournament-event.model';
import * as moment from 'moment';
import {PricingMethod} from '../model/pricing-method.enum';
import {Personnel} from './model/personnel.model';
import {CheckInType} from '../model/check-in-type.enum';
import {EligibilityRestriction} from './model/eligibility-restriction.enum';

export class Tournament {
  id: number;
  name: string;
  venueName: string;
  streetAddress: string;
  city: string;
  state: string;
  zipCode: string;
  startDate: Date;
  endDate: Date;
  starLevel: number;
  // contact information
  contactName: string;
  email: string;
  phone: string;

  // total number of entries
  numEntries: number;
  // number of event spots taken vs all that are available
  numEventEntries: number;
  // maximum number of event entries
  maxNumEventEntries: number;

  configuration: TournamentConfiguration;

  // total of all prize money
  totalPrizeMoney: number;

  // events in the tournament
  events: TournamentEvent [];

  ready: boolean;

  // convert dates from string to date objects
  static convert(tournament: Tournament): Tournament {
    const dateUtils = new DateUtils();
    // convert dates only from string to Date objects
    const configuration = tournament.configuration;
    const convertedConfiguration = {
      ...configuration,
      eligibilityDate: dateUtils.convertFromString(tournament.configuration?.eligibilityDate),
      lateEntryDate: dateUtils.convertFromString(tournament.configuration?.lateEntryDate),
      entryCutoffDate: dateUtils.convertFromString(tournament.configuration?.entryCutoffDate),
      refundDate: dateUtils.convertFromString(tournament.configuration?.refundDate)
    };
    const retValue = {
      ...tournament,
      startDate: dateUtils.convertFromString(tournament.startDate),
      endDate: dateUtils.convertFromString(tournament.endDate),
      configuration: convertedConfiguration
    };
    // console.log ('convert ', retValue);
    return retValue;
  }

  /**
   * Converts form values to Tournament object
   * @param formValues form values
   */
  static toTournament(formValues: any): Tournament {
    const dateUtils = new DateUtils();
    const tournament: Tournament = new Tournament();
    tournament.id = formValues.id;
    tournament.name = formValues.name;

    tournament.venueName = formValues.venueName;
    tournament.streetAddress = formValues.streetAddress;
    tournament.city = formValues.city;
    tournament.state = formValues.state;
    tournament.zipCode = formValues.zipCode;
    tournament.starLevel = formValues.starLevel;
    tournament.startDate = dateUtils.convertFromLocalToUTCDate(formValues.startDate);
    tournament.endDate = dateUtils.convertFromLocalToUTCDate(formValues.endDate);

    tournament.contactName = formValues.contactName;
    tournament.email = formValues.email;
    tournament.phone = formValues.phone;
    tournament.totalPrizeMoney = formValues.totalPrizeMoney;
    tournament.ready = formValues.ready;
    // load configuration object separately
    const configuration = new TournamentConfiguration();
    tournament.configuration = configuration;
    configuration.eligibilityDate = dateUtils.convertFromLocalToUTCDate(formValues.eligibilityDate);
    configuration.entryCutoffDate = dateUtils.convertFromLocalToUTCDate(formValues.entryCutoffDate);
    configuration.lateEntryDate = dateUtils.convertFromLocalToUTCDate(formValues.lateEntryDate);
    configuration.refundDate = dateUtils.convertFromLocalToUTCDate(formValues.refundDate);
    configuration.blankEntryUrl = formValues.blankEntryUrl;
    configuration.maxDailyEvents = formValues.maxDailyEvents;
    configuration.maxTournamentEvents = formValues.maxTournamentEvents;
    configuration.numberOfTables = formValues.numberOfTables;
    configuration.tournamentType = formValues.tournamentType;
    configuration.pricingMethod = formValues.pricingMethod;
    configuration.registrationFee = formValues.registrationFee;
    configuration.lateEntryFee = formValues.lateEntryFee;
    configuration.checkInType = formValues.checkInType;
    configuration.monitoredTables = formValues.monitoredTables;
    configuration.eligibilityRestriction = formValues.eligibilityRestriction;
    configuration.ballType = formValues.ballType;
// console.log ('toTournament', tournament);
    return tournament;
  }

  /**
   * Adjust dates to be in UTC zone
   */
  static prepareForSaving (tournament: Tournament): Tournament {
    const dateUtils = new DateUtils();
    const convertedConfiguration: TournamentConfiguration = {
      ...tournament.configuration,
      eligibilityDate : dateUtils.convertFromLocalToUTCDate(tournament.configuration.eligibilityDate),
      entryCutoffDate : dateUtils.convertFromLocalToUTCDate(tournament.configuration.entryCutoffDate),
      lateEntryDate : dateUtils.convertFromLocalToUTCDate(tournament.configuration.lateEntryDate),
      refundDate : dateUtils.convertFromLocalToUTCDate(tournament.configuration.refundDate)
    };

    return {
      ...tournament,
      startDate: dateUtils.convertFromLocalToUTCDate(tournament.startDate),
      endDate: dateUtils.convertFromLocalToUTCDate(tournament.endDate),
      configuration: convertedConfiguration
    };
  }

  /**
   * Sets some sensible defaults so it is easier to add new tournament
   */
  static makeDefault() {
    const tournament: Tournament = new Tournament();
    tournament.starLevel = 2;

    // find nearest Saturday 4 months from today
    let proposedDate = moment().add(4, 'months');
    const weekday = proposedDate.get('weekday');
    const addDays = 6 - weekday;  // 6 is saturday
    proposedDate = (addDays < 0) ? proposedDate.subtract(addDays, 'days') :
      ((addDays > 0) ? proposedDate.add(addDays, 'days')
        : proposedDate);

    const startEndDate = proposedDate.toDate();
    const dateUtils = new DateUtils();
    tournament.startDate = dateUtils.convertFromLocalToUTCDate(startEndDate);
    tournament.endDate = dateUtils.convertFromLocalToUTCDate(startEndDate);
    tournament.configuration = new TournamentConfiguration();
    const entryCutoffDate = proposedDate.subtract(1, 'weeks').toDate();
    tournament.configuration.entryCutoffDate = dateUtils.convertFromLocalToUTCDate(entryCutoffDate);
    const lateEntryDate = proposedDate.subtract(1, 'weeks').toDate();
    tournament.configuration.lateEntryDate = dateUtils.convertFromLocalToUTCDate(lateEntryDate);
    tournament.configuration.refundDate = dateUtils.convertFromLocalToUTCDate(lateEntryDate);
    const eligibilityDate = proposedDate.subtract(2, 'weeks').toDate();
    tournament.configuration.eligibilityDate = dateUtils.convertFromLocalToUTCDate(eligibilityDate);

    tournament.configuration.maxDailyEvents = 0;
    tournament.configuration.maxTournamentEvents = 0;
    tournament.configuration.numberOfTables = 10;
    tournament.configuration.tournamentType = 'RatingsRestricted'; // rating restricted
    tournament.configuration.pricingMethod = PricingMethod.STANDARD;
    tournament.configuration.registrationFee = 0;
    tournament.configuration.lateEntryFee = 0;
    tournament.configuration.personnelList = [];
    tournament.configuration.checkInType = CheckInType.DAILY;
    tournament.configuration.monitoredTables = null;
    tournament.configuration.eligibilityRestriction = EligibilityRestriction.OPEN;
    tournament.configuration.ballType = "N/A";
    tournament.ready = false;
    return tournament;
  }

  /**
   * Makes a sensible copy of this tournament
   * @param tournament
   */
  static makeTournamentCopy(tournament: Tournament) {
    if (tournament == null) {
      return new Tournament();
    }
    const startDate = moment(tournament.startDate);
    const endDate = moment(tournament.endDate);
    const diffDays = endDate.diff(startDate, 'days');

    // move to next year
    let proposedStartDate = moment(tournament.startDate).add(1, 'years');
    const weekday = proposedStartDate.get('weekday');
    const addDays = 6 - weekday;  // 6 is saturday
    proposedStartDate = (addDays < 0) ? proposedStartDate.subtract(addDays, 'days') :
      ((addDays > 0) ? proposedStartDate.add(addDays, 'days')
        : proposedStartDate);

    // same number of days long.
    const proposedEndDate = moment(proposedStartDate).add(diffDays, 'days');
    const newStartDate = proposedStartDate.toDate();
    const newEndDate = proposedEndDate.toDate();

    const cutoffDateDiff = (tournament?.configuration?.entryCutoffDate)
          ? startDate.diff(moment(tournament.configuration.entryCutoffDate), 'days')
          : 7;
    const entryCutoffDate = moment(proposedStartDate).subtract(cutoffDateDiff, 'days').toDate();

    const lateEntryDateDiff = (tournament?.configuration?.lateEntryDate)
      ? startDate.diff(moment(tournament.configuration.lateEntryDate), 'days')
      : 7;
    const lateEntryDate = moment(proposedStartDate).subtract(lateEntryDateDiff, 'days').toDate();

    const refundDateDiff = (tournament?.configuration?.refundDate)
      ? startDate.diff(moment(tournament.configuration.refundDate), 'days')
      : 7;
    const refundDate = moment(proposedStartDate).subtract(refundDateDiff, 'days').toDate();

    const eligibilityDateDiff = (tournament?.configuration?.eligibilityDate)
      ? startDate.diff(moment(tournament.configuration.eligibilityDate), 'days')
      : 14;
    const eligibilityDate = moment(proposedStartDate).subtract(eligibilityDateDiff, 'days').toDate();

    const convertedConfiguration = {
      ...tournament.configuration,
      eligibilityDate: eligibilityDate,
      lateEntryDate: lateEntryDate,
      entryCutoffDate: entryCutoffDate,
      refundDate: refundDate
    };
    const dateUtils = new DateUtils();
    const clonedTournament: Tournament = {
      ...tournament,
      id: null,
      name: '--clone--' + tournament.name + ' Copy',
      startDate: dateUtils.convertFromLocalToUTCDate(newStartDate),
      endDate: dateUtils.convertFromLocalToUTCDate(newEndDate),
      configuration: convertedConfiguration,
      numEntries: 0,
      numEventEntries: 0,
      ready: false
    };
    return clonedTournament;
  }

  static cloneTournament (tournament: Tournament): Tournament {
    // const clonedTournament: Tournament = JSON.parse(JSON.stringify(tournament));
    // in case the tournament configuration has some new properties get their default values and override with
    // what we received,
    // also preserve the dates
    const clonedConfiguration: TournamentConfiguration = {
      ...new TournamentConfiguration(),
      ...tournament?.configuration,
      personnelList: tournament?.configuration?.personnelList ?? [],
      eligibilityRestriction: tournament?.configuration?.eligibilityRestriction ?? EligibilityRestriction.OPEN,
      ballType: tournament?.configuration?.ballType ?? "N/A"
    };
    const clonedTournament: Tournament = {
      ...tournament,
      configuration: clonedConfiguration
    };
    return clonedTournament;
  }

}

// this part of configuration is not queryable so we keept it in separate object
export class TournamentConfiguration {
  // Late entry date Date to start charging late fees
  lateEntryDate: Date;
  // Entry cutoff date Date to stop accepting on-line entries
  entryCutoffDate: Date;
  // Rating cutoff date Rating cutoff date for event eligibility
  eligibilityDate: Date;
  // Date to stop issuing refunds for withdrawal from event or tournament
  refundDate: Date;
  // url where blank entry form is located
  blankEntryUrl: string;
  // maximum events player can enter per day
  maxDailyEvents: number;
  // maximum events player can enter in a tournament
  maxTournamentEvents: number;
  // number of tables available for play
  numberOfTables: number;
  // tournament type - rating restricted, round robin, teams
  tournamentType: string;
  // some tournaments have
  registrationFee: number;
  // fee for late entry
  lateEntryFee: number;
  // determines how to calculate total due
  pricingMethod: PricingMethod;
  // list of personnel showing their role at the tournament
  personnelList: Personnel[] = [];
  // type of check in for the tournament
  checkInType: CheckInType;
  // comma separated list of tables that will have monitors e.g. show court tables
  monitoredTables: string;
  // who can play in the tournament
  eligibilityRestriction: EligibilityRestriction = EligibilityRestriction.OPEN;
  // ball type
  ballType: string;
}
