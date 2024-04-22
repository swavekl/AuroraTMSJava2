import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {AuthGuard} from '../../guards/auth.guard';
import {ProfileCompleteGuard} from '../../guards/profile-complete.guard';
import {TournamentListContainerComponent} from './tournament-list/tournament-list-container.component';
import {TournamentViewContainerComponent} from './tournament-view/tournament-view-container.component';
import {TournamentPlayersListContainerComponent} from './tournament-players-list/tournament-players-list-container.component';
import {TournamentPlayersListBigContainerComponent} from './tournament-players-list-big/tournament-players-list-big-container.component';


const routes: Routes = [
  {
    path: '',
    component: TournamentListContainerComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'view/:id',
    component: TournamentViewContainerComponent,
    canActivate: [AuthGuard, ProfileCompleteGuard]
  },
  {
    path: 'playerlist/:id',
    component: TournamentPlayersListContainerComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'playerlistbig/:id',
    component: TournamentPlayersListBigContainerComponent,
    canActivate: [AuthGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TournamentRoutingModule { }
