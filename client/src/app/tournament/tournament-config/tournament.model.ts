import {DateUtils} from '../../shared/date-utils';
import {TournamentEvent} from './tournament-event.model';
import * as moment from 'moment';
import {PricingMethod} from '../model/pricing-method.enum';

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

  // events in the tournament
  events: TournamentEvent [];

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
// console.log ('toTournament', tournament);
    return tournament;
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
    return tournament;
  }

  /**
   * Makes a sensible clone of this tournament
   * @param tournament
   */
  static cloneTournament(tournament: Tournament) {
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
    const newEndDate = proposedStartDate.toDate();

    const dateUtils = new DateUtils();
    const entryCutoffDate = proposedStartDate.subtract(1, 'weeks').toDate();
    const lateEntryDate = proposedStartDate.subtract(1, 'weeks').toDate();
    const refundDate = lateEntryDate;
    const eligibilityDate = proposedStartDate.subtract(2, 'weeks').toDate();

    const convertedConfiguration = {
      ...tournament.configuration,
      eligibilityDate: dateUtils.convertFromString(eligibilityDate),
      lateEntryDate: dateUtils.convertFromString(lateEntryDate),
      entryCutoffDate: dateUtils.convertFromString(entryCutoffDate),
      refundDate: dateUtils.convertFromString(refundDate)
    };
    const clonedTournament: Tournament = {
      ...tournament,
      id: null,
      name: null,
      startDate: dateUtils.convertFromLocalToUTCDate(newStartDate),
      endDate: dateUtils.convertFromLocalToUTCDate(newEndDate),
      configuration: convertedConfiguration
    };

    return clonedTournament;
  }

}

// this part of configuration is not queryable so we keept it in separate object
class TournamentConfiguration {
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

}
