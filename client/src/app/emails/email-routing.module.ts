import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {EmailContainerComponent} from './email/email-container.component';

const routes: Routes = [
  { path: ':tournamentId/:tournamentName', component: EmailContainerComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class EmailRoutingModule { }
