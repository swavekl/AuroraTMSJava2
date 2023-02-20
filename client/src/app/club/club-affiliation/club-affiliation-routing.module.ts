import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {AuthGuard} from '../../guards/auth.guard';
import {UserRoles} from '../../user/user-roles.enum';
import {ClubAffiliationApplicationListComponent} from './club-affiliation-application-list/club-affiliation-application-list.component';
import {
  ClubAffiliationApplicationContainerComponent
} from './club-affiliation-application/club-affiliation-application-container.component';

const routes: Routes = [{
  path: 'list',
  component: ClubAffiliationApplicationListComponent,
  canActivate: [AuthGuard],
  data: {
    roles: [UserRoles.ROLE_ADMINS, UserRoles.ROLE_USATT_CLUB_MANAGERS, UserRoles.ROLE_TOURNAMENT_DIRECTORS]
  }
},
  {
    path: 'edit/:id',
    component: ClubAffiliationApplicationContainerComponent,
    canActivate: [AuthGuard],
    data: {
      roles: [UserRoles.ROLE_ADMINS, UserRoles.ROLE_USATT_CLUB_MANAGERS, UserRoles.ROLE_TOURNAMENT_DIRECTORS]
    }
  },
  {
    path: 'create/:id',
    component: ClubAffiliationApplicationContainerComponent,
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
export class ClubAffiliationRoutingModule {
}
