import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {AuthGuard} from '../guards/auth.guard';
import {UserRoles} from '../user/user-roles.enum';
import {RefereeTournamentListContainerComponent} from './referee-tournament-list/referee-tournament-list-container.component';

const routes: Routes = [
  {
    path: '', component: RefereeTournamentListContainerComponent,
    canActivate: [AuthGuard],
    data: {
      roles: [UserRoles.ROLE_ADMINS, UserRoles.ROLE_REFEREES]
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class RefereeDashboardRoutingModule { }
