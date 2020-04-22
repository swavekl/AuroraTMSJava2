import {NgModule} from '@angular/core';
import {Routes, RouterModule} from '@angular/router';
import {TournamentConfigListContainerComponent} from './tournament-config-list/tournament-config-list-container.component';
import {TournamentConfigEditContainerComponent} from './tournament-config-edit/tournament-config-edit-container.component';


const routes: Routes = [
  {
    path: '',
    component: TournamentConfigListContainerComponent
  },
  {
    path: 'edit/:id',
    component: TournamentConfigEditContainerComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TournamentConfigRoutingModule {
}
