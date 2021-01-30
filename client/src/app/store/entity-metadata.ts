import {EntityMetadataMap} from '@ngrx/data';
import {TournamentInfo} from '../tournament/tournament/tournament-info.model';

const entityMetadata: EntityMetadataMap = {
  Tournament: {},
  TournamentInfo: {
    filterFn: (entities: TournamentInfo[], pattern: { states: string[] }) => {
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
        return inRegion;
      });
    }
  },
  TournamentEvent: {},
  TournamentEntry: {}
};

// because the plural of "hero" is not "heros"
const pluralNames = {};

export const entityConfig = {
  entityMetadata,
  pluralNames
};
