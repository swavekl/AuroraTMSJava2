import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {AuthGuard} from '../guards/auth.guard';
import {ProfileEditContainerComponent} from './profile-edit/profile-edit-container.component';
import {ProfileEditStartComponent} from './profile-edit-start/profile-edit-start.component';
import {ProfileAddByTdContainerComponent} from './profile-add-by-td/profile-add-by-td-container.component';
import {OnBoardCompleteComponent} from './on-board-complete/on-board-complete.component';

const profileRoutes: Routes = [
  {
    path: 'edit/:profileId',
    component: ProfileEditContainerComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'start',
    component: ProfileEditStartComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'addbytd/:tournamentId',
    component: ProfileAddByTdContainerComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'onboardcomplete',
    component: OnBoardCompleteComponent,
    canActivate: [AuthGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(profileRoutes)],
  exports: [RouterModule]
})
export class ProfileRoutingModule { }
