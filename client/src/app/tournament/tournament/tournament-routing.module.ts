import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {TournamentListContainerComponent} from './tournament-list/tournament-list-container.component';
import {TournamentViewContainerComponent} from './tournament-view/tournament-view-container.component';
import {AuthGuard} from '../../guards/auth.guard';


const routes: Routes = [
  {
    path: '',
    component: TournamentListContainerComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'view/:id',
    component: TournamentViewContainerComponent,
    canActivate: [AuthGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TournamentRoutingModule { }
