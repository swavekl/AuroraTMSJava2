import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {TournamentListContainerComponent} from './tournament-list/tournament-list-container.component';
import {TournamentViewContainerComponent} from './tournament-view/tournament-view-container.component';


const routes: Routes = [
  {
    path: '',
    component: TournamentListContainerComponent
  },
  {
    path: 'view/:id',
    component: TournamentViewContainerComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TournamentRoutingModule { }
