import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import {RouterModule, Routes} from '@angular/router';
import {FlexLayoutModule} from '@angular/flex-layout';

import {TournamentListContainerComponent} from './tournament-list/tournament-list-container.component';
import {TournamentListComponent} from './tournament-list/tournament-list.component';
import {TournamentEditComponent} from './tournament-edit/tournament-edit.component';
import {AuthGuard} from '../guards/auth.guard';
import { TournamentEditContainerComponent } from './tournament-edit/tournament-edit-container.component';

const tournamentRoutes: Routes = [
  {
    path: 'tournaments',
    component: TournamentListContainerComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'tournaments/edit/:id',
    component: TournamentEditContainerComponent,
    canActivate: [AuthGuard]
  }
];

@NgModule({
  declarations: [
    TournamentListContainerComponent,
    TournamentListComponent,
    TournamentEditComponent,
    TournamentEditContainerComponent
  ],
  imports: [
    RouterModule.forChild(tournamentRoutes),
    CommonModule,
    BrowserAnimationsModule,
    MatButtonModule,
    MatCardModule,
    MatInputModule,
    MatListModule,
    MatToolbarModule,
    MatIconModule,
    MatGridListModule,
    MatProgressBarModule,
    FlexLayoutModule
  ],
  exports: [
    TournamentListComponent
  ]
})
export class TournamentModule {
}
