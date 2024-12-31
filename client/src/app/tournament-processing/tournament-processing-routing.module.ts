import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {UserRoles} from '../user/user-roles.enum';
import {TournamentProcessingListComponent} from './tournament-processing-list/tournament-processing-list.component';
import {
  TournamentProcessingDetailContainerComponent
} from './tournament-processing-detail/tournament-processing-detail-container.component';
import {AuthGuard} from '../guards/auth.guard';
import {VerifyMembershipsContainerComponent} from './verify-memberships/verify-memberships-container.component';

const routes: Routes = [
  {
    path: '', component: TournamentProcessingListComponent,
    data: {
      roles: [UserRoles.ROLE_USATT_TOURNAMENT_MANAGERS, UserRoles.ROLE_ADMINS]
    },
    canActivate: [AuthGuard]
  },
  {
    path: 'detail/:id', component: TournamentProcessingDetailContainerComponent,
    data: {
      roles: [UserRoles.ROLE_USATT_TOURNAMENT_MANAGERS, UserRoles.ROLE_ADMINS, UserRoles.ROLE_TOURNAMENT_DIRECTORS]
    },
    canActivate: [AuthGuard]
  },
  {
    path: 'detail/submit/:tournamentId/:tournamentName', component: TournamentProcessingDetailContainerComponent,
    data: {
      roles: [UserRoles.ROLE_USATT_TOURNAMENT_MANAGERS, UserRoles.ROLE_ADMINS, UserRoles.ROLE_TOURNAMENT_DIRECTORS]
    },
    canActivate: [AuthGuard]
  },
  {
    path: 'verifymemberships/:tournamentId/:tournamentName', component: VerifyMembershipsContainerComponent,
    data: {
      roles: [UserRoles.ROLE_ADMINS, UserRoles.ROLE_TOURNAMENT_DIRECTORS]
    },
    canActivate: [AuthGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TournamentProcessingRoutingModule {
}
