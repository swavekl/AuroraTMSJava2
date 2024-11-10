import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {AdminRatingsContainerComponent} from './admin-ratings/admin-ratings-container.component';
import {AuthGuard} from '../guards/auth.guard';
import {UserListComponent} from './user-list/user-list.component';

const routes: Routes = [
  {
    path: '', component: AdminRatingsContainerComponent, canActivate: [AuthGuard]
  },
  {
    path: 'userlist', component: UserListComponent, canActivate: [AuthGuard]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AdminRoutingModule { }
