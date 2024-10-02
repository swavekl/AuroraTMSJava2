import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {UmpireManagementContainerComponent} from './umpire-management/umpire-management-container.component';

const routes: Routes = [
  {
    path: '', component: UmpireManagementContainerComponent
  }
];
@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class UmpiresRoutingModule { }
