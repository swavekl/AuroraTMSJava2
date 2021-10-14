import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {TodayComponent} from './today/today.component';
import {CheckinCommunicateComponent} from './checkincommunicate/checkin-communicate.component';
import {AuthGuard} from '../guards/auth.guard';
import {CheckinCommunicateContainerComponentComponent} from './checkincommunicate/checkin-communicate-container-component.component';

const routes: Routes = [
  {
    path: 'landing/:tournamentId/:tournamentDay/:tournamentEntryId',
    component: TodayComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'checkincommunicate/:tournamentId/:tournamentDay/:tournamentEntryId',
    component: CheckinCommunicateContainerComponentComponent,
    canActivate: [AuthGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TodayRoutingModule {
}
