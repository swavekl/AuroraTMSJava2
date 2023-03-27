import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {AuthGuard} from '../../guards/auth.guard';
import {DrawsViewEventsContainerComponent} from './draws-view-events/draws-view-events-container.component';
import {DrawsViewDetailContainerComponent} from './draws-view-detail/draws-view-detail-container.component';

const routes: Routes = [
  {
    path: ':tournamentId',
    component: DrawsViewEventsContainerComponent,
    canActivate: [AuthGuard]
  },
  {
    path: ':tournamentId/details/:eventId',
    component: DrawsViewDetailContainerComponent,
    canActivate: [AuthGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DrawsViewRoutingModule { }
