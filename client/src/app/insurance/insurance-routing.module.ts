import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {AuthGuard} from '../guards/auth.guard';
import {UserRoles} from '../user/user-roles.enum';
import {InsuranceListComponent} from './insurance-list/insurance-list.component';
import {InsuranceContainerComponent} from './insurance/insurance-container.component';

const routes: Routes = [
  {
    path: 'list', component: InsuranceListComponent,
    canActivate: [AuthGuard],
    data: {
      roles: [UserRoles.ROLE_ADMINS, UserRoles.ROLE_USATT_OFFICIALS, UserRoles.ROLE_TOURNAMENT_DIRECTORS]
    }
  },
  {
    path: 'edit/:id', component: InsuranceContainerComponent,
    canActivate: [AuthGuard],
    data: {
      roles: [UserRoles.ROLE_ADMINS, UserRoles.ROLE_USATT_OFFICIALS, UserRoles.ROLE_TOURNAMENT_DIRECTORS]
    }
  },
  {
    path: 'create/:id', component: InsuranceContainerComponent,
    canActivate: [AuthGuard],
    data: {
      roles: [UserRoles.ROLE_ADMINS, UserRoles.ROLE_USATT_OFFICIALS, UserRoles.ROLE_TOURNAMENT_DIRECTORS]
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class InsuranceRoutingModule { }
