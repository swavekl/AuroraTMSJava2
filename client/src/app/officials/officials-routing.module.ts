import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {OfficialsListComponent} from './officials-list/officials-list.component';
import {OfficialEditContainerComponent} from './official-edit/official-edit-container.component';
import {AuthGuard} from '../guards/auth.guard';
import {UserRoles} from '../user/user-roles.enum';

const routes: Routes = [
  {
    path: '',
    component: OfficialsListComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'create/:profileId',
    component: OfficialEditContainerComponent,
    canActivate: [AuthGuard],
    data: {
      roles: [UserRoles.ROLE_ADMINS, UserRoles.ROLE_USATT_MATCH_OFFICIALS_MANAGERS]
    }
  },
  {
    path: 'edit/:officialId',
    component: OfficialEditContainerComponent,
    canActivate: [AuthGuard],
    data: {
      roles: [UserRoles.ROLE_ADMINS, UserRoles.ROLE_USATT_MATCH_OFFICIALS_MANAGERS]
    }
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class OfficialsRoutingModule {
}
