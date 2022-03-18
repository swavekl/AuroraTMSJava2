import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatListModule} from '@angular/material/list';
import {MatTableModule} from '@angular/material/table';
import {MatPaginatorModule} from '@angular/material/paginator';
import {MatSortModule} from '@angular/material/sort';
import {MatToolbarModule} from '@angular/material/toolbar';
import {FormsModule} from '@angular/forms';

import {TournamentProcessingRoutingModule} from './tournament-processing-routing.module';
import {TournamentProcessingListComponent} from './tournament-processing-list/tournament-processing-list.component';
import {TournamentProcessingDetailComponent} from './tournament-processing-detail/tournament-processing-detail.component';
import {
  TournamentProcessingDetailContainerComponent
} from './tournament-processing-detail/tournament-processing-detail-container.component';
import {MatInputModule} from '@angular/material/input';
import {MatIconModule} from '@angular/material/icon';
import {FlexModule} from '@angular/flex-layout';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';


@NgModule({
  declarations: [
    TournamentProcessingListComponent,
    TournamentProcessingDetailComponent,
    TournamentProcessingDetailContainerComponent
  ],
    imports: [
        CommonModule,
        TournamentProcessingRoutingModule,
        MatListModule,
        MatTableModule,
        MatPaginatorModule,
        MatSortModule,
        MatToolbarModule,
        MatInputModule,
        FormsModule,
        MatIconModule,
        FlexModule,
        MatButtonModule,
        MatCardModule
    ]
})
export class TournamentProcessingModule {
}
