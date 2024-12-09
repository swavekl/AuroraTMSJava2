import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {UmpireManagementContainerComponent} from './umpire-management/umpire-management-container.component';
import {UmpireBookComponent} from './umpire-book/umpire-book.component';

const routes: Routes = [
  {
    path: 'list/:tournamentId', component: UmpireManagementContainerComponent
  },
  {
    path: 'view', component: UmpireBookComponent
  }
];
@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class UmpiresRoutingModule { }
