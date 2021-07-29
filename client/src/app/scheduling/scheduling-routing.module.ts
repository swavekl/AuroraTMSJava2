import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ScheduleManageContainerComponent} from './manage/schedule-manage-container.component';
import {AuthGuard} from '../guards/auth.guard';

const routes: Routes = [
  {
    path: ':tournamentId',
    component: ScheduleManageContainerComponent,
    canActivate: [AuthGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SchedulingRoutingModule {
}
