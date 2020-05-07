import {DateUtils} from '../../shared/date-utils';
import {TournamentEvent} from './tournament-event.model';

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
      entryCutoffDate: dateUtils.convertFromString(tournament.configuration?.entryCutoffDate)
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
    configuration.blankEntryUrl = formValues.blankEntryUrl;
    configuration.maxDailyEvents = formValues.maxDailyEvents;
    configuration.maxTournamentEvents = formValues.maxTournamentEvents;
    configuration.numberOfTables = formValues.numberOfTables;
// console.log ('toTournament', tournament);
    return tournament;
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
  // url where blank entry form is located
  blankEntryUrl: string;
  // maximum events player can enter per day
  maxDailyEvents: number;
  // maximum events player can enter in a tournament
  maxTournamentEvents: number;
  // number of tables available for play
  numberOfTables: number;
}
