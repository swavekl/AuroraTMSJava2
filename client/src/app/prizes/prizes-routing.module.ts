import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {PrizeListComponent} from './prize-list/prize-list.component';
import {AuthGuard} from '../guards/auth.guard';
import {UserRoles} from '../user/user-roles.enum';
import {PrizeListContainerComponent} from './prize-list/prize-list-container.component';

const restrictedAccessRoles = [
  UserRoles.ROLE_TOURNAMENT_DIRECTORS, UserRoles.ROLE_ADMINS
];

const routes: Routes = [
  {
    path: '',
    component: PrizeListContainerComponent,
    canActivate: [AuthGuard],
    data: {
      roles: restrictedAccessRoles
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PrizesRoutingModule { }
