import {EntityMetadataMap} from '@ngrx/data';
import {TournamentInfo} from '../tournament/model/tournament-info.model';
import {DateUtils} from '../shared/date-utils';

const entityMetadata: EntityMetadataMap = {
  Tournament: {},
  TournamentInfo: {
    filterFn: (entities: TournamentInfo[],
               pattern: { states: string[], startDate: Date, endDate: Date }) => {
      return entities.filter(entity => {
        let inRegion = false;
        if (pattern.states !== null) {
          for (const state of pattern.states) {
            if (state === entity.state) {
              inRegion = true;
              break;
            }
          }
        } else {
          inRegion = true;
        }

        // don't show very old tournaments - only recent ones and future
        let inDateRange = true;
        const dateUtils: DateUtils = new DateUtils();
        const tournamentStartDate = dateUtils.convertFromString(entity.startDate);
        if (tournamentStartDate != null) {
          if (pattern.startDate != null) {
            inDateRange = dateUtils.isDateBefore(pattern.startDate, tournamentStartDate);
          } else if (pattern.startDate != null && pattern.endDate != null) {
            inDateRange = dateUtils.isDateInRange(tournamentStartDate, pattern.startDate, pattern.endDate);
            // inDateRange =
            //   dateUtils.isDateBefore(pattern.startDate, tournamentStartDate) &&
            //   dateUtils.isDateBefore(tournamentStartDate, pattern.endDate);
          }
        }
        // console.log ('inDateRange ' + inDateRange + ' ' + entity.startDate + ' vs ' + pattern.startDate);

        return inRegion && inDateRange;
      });
    }
  },
  TournamentEvent: {},
  TournamentEntry: {},
  TournamentEventEntry: {},
  TournamentEventEntryInfo: {},
  DrawItem: {},
  DoublesPair: {},
  DoublesPairInfo: {},
  MatchCard: {},
  Match: {},
  PlayerStatus: {},
  Club: {
    additionalCollectionState: {
      total: 0
    }
  },
  ClubAffiliationApplication: {
    additionalCollectionState: {
      total: 0
    }
  },
  InsuranceRequest: {
    additionalCollectionState: {
      total: 0
    }
  },
  SanctionRequest: {
    additionalCollectionState: {
      total: 0
    }
  },
  TableUsage: {},
  TournamentProcessingRequest: {
    additionalCollectionState: {
      total: 0
    }
  },
  Official: {
    additionalCollectionState: {
      total: 0
    }
  },
  Audit: {}
};

// because the plural of "hero" is not "heros"
const pluralNames = {};

export const entityConfig = {
  entityMetadata,
  pluralNames
};
