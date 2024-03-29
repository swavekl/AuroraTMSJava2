import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {AuthGuard} from '../guards/auth.guard';
import {MatchesLandingContainerComponent} from './matches-landing/matches-landing-container.component';
import {MatchesContainerComponent} from './matches/matches-container.component';
import {PlayerMatchesContainerComponent} from './player-matches/player-matches-container.component';
import {UserRoles} from '../user/user-roles.enum';
import {ScoreEntryPhoneContainerComponent} from './score-entry-phone/score-entry-phone-container.component';
import {RankingResultsContainerComponent} from './ranking-results/ranking-results-container.component';

const restrictedAccessRoles = [
  UserRoles.ROLE_TOURNAMENT_DIRECTORS, UserRoles.ROLE_ADMINS, UserRoles.ROLE_DATA_ENTRY_CLERKS, UserRoles.ROLE_UMPIRES
];
const unrestrictedAccessRoles = [
  UserRoles.ROLE_EVERYONE
];
const routes: Routes = [
  {
    path: '',
    component: MatchesLandingContainerComponent,
    canActivate: [AuthGuard],
    data: {
      roles: restrictedAccessRoles
    }
  },
  {
    path: 'scoreentry/:tournamentId',
    component: MatchesContainerComponent,
    canActivate: [AuthGuard],
    data: {
      roles: restrictedAccessRoles
    }
  },
  {
    path: 'playermatches/:tournamentId/:tournamentDay/:tournamentEntryId/:matchCardId',
    component: PlayerMatchesContainerComponent,
    canActivate: [AuthGuard],
    data: {
      roles: unrestrictedAccessRoles
    }
  },
  {
    path: 'scoreentryphone/:tournamentId/:tournamentDay/:tournamentEntryId/:matchCardId/:matchIndex',
    component: ScoreEntryPhoneContainerComponent,
    canActivate: [AuthGuard],
    data: {
      roles: unrestrictedAccessRoles
    }
  },
  {
    path: 'rankingresults/:matchCardId',
    component: RankingResultsContainerComponent,
    canActivate: [AuthGuard],
    data: {
      roles: unrestrictedAccessRoles
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class MatchesRoutingModule { }
