import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {AdminRatingsContainerComponent} from './admin-ratings/admin-ratings-container.component';
import {AuthGuard} from '../guards/auth.guard';

const routes: Routes = [{ path: '', component: AdminRatingsContainerComponent, canActivate: [AuthGuard] }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AdminRoutingModule { }
