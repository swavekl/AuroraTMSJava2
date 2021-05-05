import {NgModule} from '@angular/core';
import {Routes, RouterModule} from '@angular/router';
import {AuthGuard} from '../guards/auth.guard';
import {DrawsContainerComponent} from './draws/draws-container.component';
import {SingleEliminationBracketComponent} from './single-elimination-bracket/single-elimination-bracket.component';

const routes: Routes = [
  {
    path: ':tournamentId',
    component: DrawsContainerComponent,
    canActivate: [AuthGuard]
  },
  {
    path: ':tournamentId/singleelimination/:eventId',
    component: SingleEliminationBracketComponent,
    canActivate: [AuthGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DrawsRoutingModule {
}
