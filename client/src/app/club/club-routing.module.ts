import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {AuthGuard} from '../guards/auth.guard';
import {ClubListComponent} from './club-list/club-list.component';
import {UserRoles} from '../user/user-roles.enum';
import {ClubAffiliationApplicationListComponent} from './club-affiliation-application-list/club-affiliation-application-list.component';
// tslint:disable-next-line:max-line-length
import {ClubAffiliationApplicationContainerComponent} from './club-affiliation-application/club-affiliation-application-container.component';

const routes: Routes = [
  {
    path: 'list',
    component: ClubListComponent,
    canActivate: [AuthGuard],
    data: {
      roles: [UserRoles.ROLE_ADMINS, UserRoles.ROLE_USATT_CLUB_MANAGERS, UserRoles.ROLE_TOURNAMENT_DIRECTORS]
    }
  },
  {
    path: 'affiliationlist',
    component: ClubAffiliationApplicationListComponent,
    canActivate: [AuthGuard],
    data: {
      roles: [UserRoles.ROLE_ADMINS, UserRoles.ROLE_USATT_CLUB_MANAGERS, UserRoles.ROLE_TOURNAMENT_DIRECTORS]
    }
  },
  {
    path: 'affiliationedit/:id',
    component: ClubAffiliationApplicationContainerComponent,
    canActivate: [AuthGuard],
    data: {
      roles: [UserRoles.ROLE_ADMINS, UserRoles.ROLE_USATT_CLUB_MANAGERS, UserRoles.ROLE_TOURNAMENT_DIRECTORS]
    }
  },
  {
    path: 'affiliationcreate/:id',
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
export class ClubRoutingModule {
}
