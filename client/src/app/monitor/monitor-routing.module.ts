import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {AuthGuard} from '../guards/auth.guard';
import {UserRoles} from '../user/user-roles.enum';
import {MonitorConnectContainerComponent} from './monitor-connect/monitor-connect-container.component';
import {MonitorDisplayContainerComponent} from './monitor-display/monitor-display-container.component';

const routes: Routes = [
  {
    path: '',
    component: MonitorConnectContainerComponent,
    canActivate: [AuthGuard],
    data: {
      roles: [UserRoles.ROLE_ADMINS, UserRoles.ROLE_TOURNAMENT_DIRECTORS]
    }
  },
  {
    path: 'display',
    component: MonitorDisplayContainerComponent,
    canActivate: [AuthGuard],
    data: {
      roles: [UserRoles.ROLE_ADMINS, UserRoles.ROLE_TOURNAMENT_DIRECTORS]
    }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class MonitorRoutingModule { }
