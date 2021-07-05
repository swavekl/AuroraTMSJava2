import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {AuthGuard} from '../guards/auth.guard';
import {MatchesLandingContainerComponent} from './matches-landing/matches-landing-container.component';
import {MatchesContainerComponent} from './matches/matches-container.component';
import {UserRoles} from '../user/user-roles.enum';

const routes: Routes = [
  {
    path: '',
    component: MatchesLandingContainerComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'scoreentry/:tournamentId',
    component: MatchesContainerComponent,
    canActivate: [AuthGuard]
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class MatchesRoutingModule { }
