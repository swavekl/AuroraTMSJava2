import {DateUtils} from '../../shared/date-utils';

export enum SanctionRequestStatus {
  New = 'New',
  Submitted = 'Submitted',
  Approved = 'Approved',
  Rejected = 'Rejected',
  Completed = 'Completed'

// Create Tournament - TD creates a tournament entry in the system
// Pay USATT - TD pays the sanctioning fee online via paypal
// USATT COVID Approval - USATT approves the COVID forms attached to the Tournament
// Sanctioning Coordinator Approval - Sanctioning coordinator approves the tournament
// Tournament GO-Live - Tournament is considered live and can accept online entries from players
}

/**
 * criteria under sanction category
 */
export class SanctionCategoryCriteria {
  name: string;
  points: number;
  selected: boolean;
  requiredForStarLevel: string;

  constructor () {
    this.name = '';
    this.points = 0;
    this.selected = false;
    this.requiredForStarLevel = '';
  }

  setValues (name: string, points: number, requiredForStarLevel: string, selected?: boolean) {
    this.name = name;
    this.points = points;
    this.selected = (selected) ? selected : false;
    this.requiredForStarLevel = requiredForStarLevel;
  }

  applyChanges (criteriaValue: any, selectOne: boolean) {
    if (selectOne) {
      this.selected = (this.points === criteriaValue);
    } else {
      this.selected = (criteriaValue !== undefined) ? criteriaValue : false;
    }
  }
}


/**
 * Sanction category
 */
export class SanctionCategory {
  title: string;

  name: string;

  // select one item in category or all that apply
  selectOne: boolean;

  selectedValue: number;

  criteria: SanctionCategoryCriteria [];

  // default constructor needed for decorator
  constructor () {
    this.title = '';
    this.name = '';
    this.selectOne = false;
    this.selectedValue = 0;
  }

  setValues (title: string, name: string, selectOne: boolean) {
    this.title = title;
    this.name = name;
    this.selectOne = selectOne;
    this.selectedValue = 0;
    return this;
  }

  setCriteria (criteria: SanctionCategoryCriteria []) {
    this.criteria = criteria;
  }

  // fills categories from formValues supplied by the html form
  applyChanges (formValues: any) {
    for (let i = 0; i < this.criteria.length; i++) {
      const htmlControlName: string  = this.getHtmlControlName(i);
      const criteriaValue = formValues[htmlControlName];
      const criterion: SanctionCategoryCriteria = this.criteria[i];
      criterion.applyChanges(criteriaValue, this.selectOne);
      if (this.selectOne && criterion.selected) {
        this.selectedValue = criteriaValue;
      }
    }
    // console.log ('in applyChanges for ' + this.name + " has selectedValue of " + this.selectedValue);
  }

  // fills category from supplied sourceCategory generic object
  fillFromSource(sourceCategory: any) {
    const criteriaSettings: any [] = sourceCategory.criteria || [];
    for (let i = 0; i < criteriaSettings.length; i++) {
      const source: any = criteriaSettings[i];
      const target: SanctionCategoryCriteria = (this.criteria.length > i) ? this.criteria[i] : null;
      const criteriaValue = (this.selectOne) ? sourceCategory.selectedValue : source.selected;
      if (source && target) {
        target.applyChanges (criteriaValue, this.selectOne);
      }
    }
    if (this.selectOne) {
      this.selectedValue = sourceCategory.selectedValue;
    }
  }

  getHtmlControlName (i: number) {
    return (this.selectOne === false) ? (this.name + i) : this.name;
  }

  getSubTotal () {
    let subTotal = 0;
    if (this.selectOne) {
      subTotal = this.selectedValue;
    } else {
      for (let i = 0; i < this.criteria.length; i++) {
        const criterion: SanctionCategoryCriteria = this.criteria[i];
        if (criterion.selected) {
          subTotal += criterion.points;
        }
      }
    }
    return subTotal;
  }
}

export class SanctionRequest {

  id: number;
  tournamentName: string;
  startDate: Date;
  endDate: Date;
  requestDate: Date;
  status: SanctionRequestStatus;

  // 0 - 5 stars
  starLevel: number;

  // regional or national coordinator
  coordinatorFirstName: string;
  coordinatorLastName: string;

  // email retrieved from frontend table
  coordinatorEmail: string;

  alternateStartDate: Date;
  alternateEndDate: Date;

  webLinkURL: string;

  venueStreetAddress: string;
  venueCity: string;
  venueState: string;
  venueZipCode: number;

  clubName: string;
  clubAffiliationExpiration: Date;

  contactPersonName: string;
  contactPersonPhone: string;
  contactPersonEmail: string;
  contactPersonStreetAddress: string;
  contactPersonCity: string;
  contactPersonState: string;
  contactPersonZip: number;

  tournamentRefereeName: string;
  tournamentRefereeRank: string;
  tournamentRefereeMembershipExpires: Date;

  tournamentDirectorName: string;

  totalPrizeMoney: number;
  sanctionFee: number;

  categories: SanctionCategory [];

  approvalRejectionNotes: string;

  constructor () {
    this.tournamentName = '';
    // this.requestContents = new SanctionRequestContents();
    this.status = SanctionRequestStatus.New;
    this.makeCategories();
  }

  // constructs default categories
  private makeCategories () {
    const lighting: SanctionCategory = new SanctionCategory ().setValues (
      'Light Strength as measured on table playing surface', 'lighting', true);
    const lightCriteria: SanctionCategoryCriteria [] = [
      this.makeSanctionCategoryCriteria ('300 Lux with fixtures at least 8 ft above the floor', 0, 'Minimum standard', true),
      this.makeSanctionCategoryCriteria ('400 Lux', 1, ''),
      this.makeSanctionCategoryCriteria ('600 Lux for feature matches', 2, '3 and up'),
      this.makeSanctionCategoryCriteria ('600 Lux', 3, ''),
      this.makeSanctionCategoryCriteria ('800 Lux for feature matches', 4, ''),
      this.makeSanctionCategoryCriteria ('800 Lux', 5, '')
    ];
    lighting.setCriteria (lightCriteria);

    const flooring: SanctionCategory = new SanctionCategory ().setValues ('Flooring Type', 'flooring', true);
    const floorCriteria: SanctionCategoryCriteria [] = [
      this.makeSanctionCategoryCriteria ('Wood floor or rubberized Mat on concrete or Tile for feature matches', 1, '3 and up'),
      this.makeSanctionCategoryCriteria ('Wood floor or rubberized Mat on concrete or Tile for all matches', 2, ''),
      this.makeSanctionCategoryCriteria ('Rubberized Mat on Wood for feature matches ', 3, ''),
      this.makeSanctionCategoryCriteria ('Rubberized Mat on Wood for all matches', 4, '')
    ];
    flooring.setCriteria (floorCriteria);

    const ceiling: SanctionCategory = new SanctionCategory ().setValues ('Ceiling Height', 'ceiling', true);
    const ceilingCriteria: SanctionCategoryCriteria [] = [
      this.makeSanctionCategoryCriteria ('8 ft ceiling height', 0, 'Minimum standard', true),
      this.makeSanctionCategoryCriteria ('10 ft ceiling height', 0, '1'),
      this.makeSanctionCategoryCriteria ('12 ft ceiling height', 1, ''),
      this.makeSanctionCategoryCriteria ('16 ft ceiling height', 2, '3 and up')
    ];
    ceiling.setCriteria (ceilingCriteria);

    const courtSize: SanctionCategory = new SanctionCategory ().setValues ('Court Size', 'courtSize', true);
    const courtSizeCriteria: SanctionCategoryCriteria [] = [
      this.makeSanctionCategoryCriteria ('30 ft length, 10 ft between tables', 0, 'Minimum standard', true),
      this.makeSanctionCategoryCriteria ('12 feet between tables', 2, '3 and up'),
      this.makeSanctionCategoryCriteria ('14 feet between tables', 4, ''),
      this.makeSanctionCategoryCriteria ('19x38 courts for feature matches', 5, '3 and up'),
      this.makeSanctionCategoryCriteria ('19x38 courts', 6, ''),
      this.makeSanctionCategoryCriteria ('23x46 courts for feature matches', 7, ''),
      this.makeSanctionCategoryCriteria ('23x46 courts', 8, '')
    ];
    courtSize.setCriteria (courtSizeCriteria);

    const tables: SanctionCategory = new SanctionCategory ().setValues ('Tables', 'tables', true);
    const tablesCriteria: SanctionCategoryCriteria [] = [
      this.makeSanctionCategoryCriteria ('Minimum standard USATT or ITTF Approved', 1, ''),
      this.makeSanctionCategoryCriteria ('No more than two models in use', 2, '3'),
      this.makeSanctionCategoryCriteria ('All models alike', 3, '4 and up'),
      this.makeSanctionCategoryCriteria ('All alike, but show table for feature matches', 4, ''),
    ];
    tables.setCriteria (tablesCriteria);

    const paraTables: SanctionCategory = new SanctionCategory ().setValues ('Para Tables', 'paraTables', false);
    const paraTablesCriteria: SanctionCategoryCriteria [] = [
      this.makeSanctionCategoryCriteria ('Para table if wheelchair players entered', 1, '')
    ];
    paraTables.setCriteria (paraTablesCriteria);

    const barriers: SanctionCategory = new SanctionCategory ().setValues ('Barriers', 'barriers', true);
    const barriersCriteria: SanctionCategoryCriteria [] = [
      this.makeSanctionCategoryCriteria ('Barrier at net between tables or at both ends of court', 2, ''),
      this.makeSanctionCategoryCriteria ('Barrier at net  between tables and at both ends of court', 3, ''),
      this.makeSanctionCategoryCriteria ('Individually barriered court for feature matches', 4, '3 and up'),
      this.makeSanctionCategoryCriteria ('All courts fully barriered', 6, '')
    ];
    barriers.setCriteria (barriersCriteria);

    const timeScheduling: SanctionCategory = new SanctionCategory ().setValues ('Time Scheduling', 'timeScheduling', true);
    const timeSchedulingCriteria: SanctionCategoryCriteria [] = [
      this.makeSanctionCategoryCriteria ('Event start times', 0, 'Minimum Standard', true),
      this.makeSanctionCategoryCriteria ('All events, all rounds', 2, '3, 4'),
      this.makeSanctionCategoryCriteria ('Published schedule for each player, all rounds', 4, '5')
    ];
    timeScheduling.setCriteria (timeSchedulingCriteria);

    const officials: SanctionCategory = new SanctionCategory ().setValues ('Officials', 'officials', true);
    const officialsCriteria: SanctionCategoryCriteria [] = [
      this.makeSanctionCategoryCriteria ('Scorekeepers for featured matches', 1, ''),
      this.makeSanctionCategoryCriteria ('Umpires for featured matches', 2, '2'),
      this.makeSanctionCategoryCriteria ('Umpires and scorekeepers for featured matches', 4, '3, 4'),
      this.makeSanctionCategoryCriteria ('Uniformed Umpires and scorekeepers for all matches', 6, '5')
    ];
    officials.setCriteria (officialsCriteria);

    const eventVariety: SanctionCategory = new SanctionCategory ().setValues ('Event Variety', 'eventVariety', false);
    const eventVarietyCriteria: SanctionCategoryCriteria [] = [
      this.makeSanctionCategoryCriteria ('Novice event', 1, ''),
      this.makeSanctionCategoryCriteria ('Women\'s event', 1, ''),
      this.makeSanctionCategoryCriteria ('Junior event', 1, ''),
      this.makeSanctionCategoryCriteria ('Para event', 1, ''),
      this.makeSanctionCategoryCriteria ('Team event', 1, ''),
      this.makeSanctionCategoryCriteria ('Doubles event', 1, '')
    ];
    eventVariety.setCriteria (eventVarietyCriteria);

    const prizeMoney: SanctionCategory = new SanctionCategory ().setValues ('Prize Money', 'prizeMoney', true);
    const prizeMoneyCriteria: SanctionCategoryCriteria [] = [
      this.makeSanctionCategoryCriteria ('$100-$400', 1, ''),
      this.makeSanctionCategoryCriteria ('$401-$1000', 3, ''),
      this.makeSanctionCategoryCriteria ('$1001-$3000', 5, ''),
      this.makeSanctionCategoryCriteria ('$3001-$6000', 7, ''),
      this.makeSanctionCategoryCriteria ('$6001-$10000', 10, ''),
      this.makeSanctionCategoryCriteria ('$10001 and up', 15, '')
    ];
    prizeMoney.setCriteria (prizeMoneyCriteria);

    const amenities: SanctionCategory = new SanctionCategory ().setValues ('Amenities', 'amenities', false);
    const amenitiesCriteria: SanctionCategoryCriteria [] = [
      this.makeSanctionCategoryCriteria ('Food & drink available inside venue', 1, ''),
      this.makeSanctionCategoryCriteria ('Player\'s lounge available', 1, ''),
      this.makeSanctionCategoryCriteria ('Officials Lounge available', 1, '')
    ];
    amenities.setCriteria (amenitiesCriteria);

    const spectatorSeating: SanctionCategory = new SanctionCategory ().setValues ('Spectator Seating', 'spectatorSeating', true);
    const spectatorSeatingCriteria: SanctionCategoryCriteria [] = [
      this.makeSanctionCategoryCriteria ('100 seats available', 1, '3'),
      this.makeSanctionCategoryCriteria ('250 seats available', 2, '4'),
      this.makeSanctionCategoryCriteria ('500 seats available', 3, '5')
    ];
    spectatorSeating.setCriteria (spectatorSeatingCriteria);

    const mediaCoverage: SanctionCategory = new SanctionCategory ().setValues ('Media Coverage', 'mediaCoverage', false);
    const mediaCoverageCriteria: SanctionCategoryCriteria [] = [
      this.makeSanctionCategoryCriteria ('Print', 2, ''),
      this.makeSanctionCategoryCriteria ('TV', 2, ''),
      this.makeSanctionCategoryCriteria ('Live streaming', 3, ''),
      this.makeSanctionCategoryCriteria ('Live streaming - USATT equipment and commentator', 3, '')
    ];
    mediaCoverage.setCriteria (mediaCoverageCriteria);

    this.categories = [
      lighting, flooring, ceiling, courtSize, tables, paraTables, barriers, timeScheduling, officials,
      eventVariety, prizeMoney, amenities, spectatorSeating, mediaCoverage
    ];
  }

  private makeSanctionCategoryCriteria (name: string, points: number, requiredForStarLevel: string, selected?: boolean) {
    const sanctionCategoryCriteria: SanctionCategoryCriteria = new SanctionCategoryCriteria();
    sanctionCategoryCriteria.name = name;
    sanctionCategoryCriteria.points = points;
    sanctionCategoryCriteria.selected = selected;
    sanctionCategoryCriteria.requiredForStarLevel = requiredForStarLevel;
    return sanctionCategoryCriteria;
  }

  // apply changes from form and perform various conversions
  applyChanges (formValues: any) {
    // apply new values to this object
    this.id = formValues.id;
    this.tournamentName = formValues.tournamentName;

    // convert dates from local to UTC
    const requestDate: Date = (formValues.requestDate != null) ? new Date (formValues.requestDate) : new Date();

    const dateUtils = new DateUtils();
    this.startDate = dateUtils.convertFromLocalToUTCDate (formValues.startDate);
    this.endDate = dateUtils.convertFromLocalToUTCDate(formValues.endDate);
    this.requestDate = dateUtils.convertFromLocalToUTCDate (requestDate);

    if (this.status == null) {
      this.status = SanctionRequestStatus.New;
    }
    // apply rating criteria
    this.starLevel = formValues.starLevel;

    this.alternateStartDate                  = formValues.alternateStartDate                ;
    this.alternateEndDate                    = formValues.alternateEndDate                  ;
    this.webLinkURL                          = formValues.webLinkURL                        ;
    this.venueStreetAddress                  = formValues.venueStreetAddress                ;
    this.venueCity                           = formValues.venueCity                         ;
    this.venueState                          = formValues.venueState                        ;
    this.venueZipCode                        = formValues.venueZipCode                      ;
    this.clubName                            = formValues.clubName                          ;
    this.clubAffiliationExpiration           = formValues.clubAffiliationExpiration         ;
    this.contactPersonName                   = formValues.contactPersonName                 ;
    this.contactPersonPhone                  = formValues.contactPersonPhone                ;
    this.contactPersonEmail                  = formValues.contactPersonEmail                ;
    this.contactPersonStreetAddress          = formValues.contactPersonStreetAddress        ;
    this.contactPersonCity                   = formValues.contactPersonCity                 ;
    this.contactPersonState                  = formValues.contactPersonState                ;
    this.contactPersonZip                    = formValues.contactPersonZip                  ;
    this.tournamentRefereeName               = formValues.tournamentRefereeName             ;
    this.tournamentRefereeRank               = formValues.tournamentRefereeRank             ;
    this.tournamentDirectorName              = formValues.tournamentDirectorName            ;
    this.totalPrizeMoney                     = formValues.totalPrizeMoney                   ;
    this.sanctionFee                         = formValues.sanctionFee                       ;
    this.tournamentRefereeMembershipExpires  = formValues.tournamentRefereeMembershipExpires;

    // now set the criteria
    for (let i = 0; i < this.categories.length; i++) {
      const category: SanctionCategory = this.categories[i];
      category.applyChanges(formValues);
    }
  }

  clone (other: SanctionRequest) {
    this.id = other.id;
    this.tournamentName = other.tournamentName;
    this.status = other.status;
    this.starLevel = other.starLevel;
    this.coordinatorFirstName = other.coordinatorFirstName;
    this.coordinatorLastName = other.coordinatorLastName;
    this.coordinatorEmail = other.coordinatorEmail;
    // convert dates from UTC to local
    const dateUtils = new DateUtils();
    this.startDate = dateUtils.convertFromUTCToLocalDate (other.startDate);
    this.endDate = dateUtils.convertFromUTCToLocalDate (other.endDate);
    this.requestDate = dateUtils.convertFromUTCToLocalDate (other.requestDate);

    this.alternateStartDate                  = other.alternateStartDate                ;
    this.alternateEndDate                    = other.alternateEndDate                  ;
    this.webLinkURL                          = other.webLinkURL                        ;
    this.venueStreetAddress                  = other.venueStreetAddress                ;
    this.venueCity                           = other.venueCity                         ;
    this.venueState                          = other.venueState                        ;
    this.venueZipCode                        = other.venueZipCode                      ;
    this.clubName                            = other.clubName                          ;
    this.clubAffiliationExpiration           = other.clubAffiliationExpiration         ;
    this.contactPersonName                   = other.contactPersonName                 ;
    this.contactPersonPhone                  = other.contactPersonPhone                ;
    this.contactPersonEmail                  = other.contactPersonEmail                ;
    this.contactPersonStreetAddress          = other.contactPersonStreetAddress        ;
    this.contactPersonCity                   = other.contactPersonCity                 ;
    this.contactPersonState                  = other.contactPersonState                ;
    this.contactPersonZip                    = other.contactPersonZip                  ;
    this.tournamentRefereeName               = other.tournamentRefereeName             ;
    this.tournamentRefereeRank               = other.tournamentRefereeRank             ;
    this.tournamentDirectorName              = other.tournamentDirectorName            ;
    this.totalPrizeMoney                     = other.totalPrizeMoney                   ;
    this.sanctionFee                         = other.sanctionFee                       ;
    this.tournamentRefereeMembershipExpires  = other.tournamentRefereeMembershipExpires;

    for (let i = 0; i < this.categories.length; i++) {
      const category: SanctionCategory = this.categories[i];
      const sourceCategory: any = (other.categories && other.categories.length > i) ? other.categories[i] : null;
      if (sourceCategory && sourceCategory.name === category.name) {
        category.fillFromSource(sourceCategory);
      }
    }
  }
}


