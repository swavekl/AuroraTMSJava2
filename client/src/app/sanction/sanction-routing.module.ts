import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {SanctionRequestListComponent} from './sanction-request-list/sanction-request-list.component';
import {SanctionRequestEditContainerComponent} from './sanction-edit/sanction-request-edit-container.component';
import {AuthGuard} from '../guards/auth.guard';
import {UserRoles} from '../user/user-roles.enum';

const routes: Routes = [
  {
    path: 'list', component: SanctionRequestListComponent,
    canActivate: [AuthGuard],
    data: {
      roles: [UserRoles.ROLE_ADMINS, UserRoles.ROLE_USATT_SANCTION_COORDINATORS, UserRoles.ROLE_TOURNAMENT_DIRECTORS]
    }
  },
  {
    path: 'create/:id', component: SanctionRequestEditContainerComponent,
    canActivate: [AuthGuard],
    data: {
      roles: [UserRoles.ROLE_ADMINS, UserRoles.ROLE_USATT_SANCTION_COORDINATORS, UserRoles.ROLE_TOURNAMENT_DIRECTORS]
    }
  },
  {
    path: 'edit/:id', component: SanctionRequestEditContainerComponent,
    canActivate: [AuthGuard],
    data: {
      roles: [UserRoles.ROLE_ADMINS, UserRoles.ROLE_USATT_SANCTION_COORDINATORS, UserRoles.ROLE_TOURNAMENT_DIRECTORS]
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SanctionRoutingModule { }
