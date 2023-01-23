import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {AdminRatingsContainerComponent} from './admin-ratings/admin-ratings-container.component';

const routes: Routes = [{ path: '', component: AdminRatingsContainerComponent }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AdminRoutingModule { }
