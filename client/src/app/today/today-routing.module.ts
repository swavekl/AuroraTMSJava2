import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {TodayComponent} from './today/today.component';
import {AuthGuard} from '../guards/auth.guard';
import {CheckinCommunicateContainerComponent} from './checkincommunicate/checkin-communicate-container.component';
import {PlayerScheduleContainerComponent} from './playerschedule/player-schedule-container.component';
import {PlayerScheduleDetailContainerComponent} from './player-schedule-detail/player-schedule-detail-container.component';

const routes: Routes = [
  {
    path: 'landing/:tournamentId/:tournamentDay/:tournamentEntryId',
    component: TodayComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'checkincommunicate/:tournamentId/:tournamentDay/:eventId',
    component: CheckinCommunicateContainerComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'playerschedule/:tournamentId/:tournamentDay/:tournamentEntryId',
    component: PlayerScheduleContainerComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'playerscheduledetail/:tournamentId/:tournamentDay/:tournamentEntryId/:matchCardId',
    component: PlayerScheduleDetailContainerComponent,
    canActivate: [AuthGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TodayRoutingModule {
}
