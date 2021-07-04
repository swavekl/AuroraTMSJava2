import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {MatchesContainerComponent} from './matches/matches-container.component';
import {AuthGuard} from '../guards/auth.guard';

const routes: Routes = [
  {
    path: ':tournamentId',
    component: MatchesContainerComponent,
    canActivate: [AuthGuard]
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class MatchesRoutingModule { }
