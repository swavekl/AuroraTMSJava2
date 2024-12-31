import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatListModule} from '@angular/material/list';
import {MatTableModule} from '@angular/material/table';
import {MatPaginatorModule} from '@angular/material/paginator';
import {MatSortModule} from '@angular/material/sort';
import {MatToolbarModule} from '@angular/material/toolbar';
import {FormsModule} from '@angular/forms';
import {MatInputModule} from '@angular/material/input';
import {MatIconModule} from '@angular/material/icon';
import {FlexLayoutModule} from 'ng-flex-layout';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatDialogModule} from '@angular/material/dialog';

import {TournamentProcessingRoutingModule} from './tournament-processing-routing.module';
import {TournamentProcessingListComponent} from './tournament-processing-list/tournament-processing-list.component';
import {TournamentProcessingDetailComponent} from './tournament-processing-detail/tournament-processing-detail.component';
import {
  TournamentProcessingDetailContainerComponent
} from './tournament-processing-detail/tournament-processing-detail-container.component';
import {GenerateReportsDialogComponent} from './generate-reports-dialog/generate-reports-dialog.component';
import {AccountModule} from '../account/account.module';
import { VerifyMembershipsComponent } from './verify-memberships/verify-memberships.component';
import { VerifyMembershipsContainerComponent } from './verify-memberships/verify-memberships-container.component';


@NgModule({
  declarations: [
    TournamentProcessingListComponent,
    TournamentProcessingDetailComponent,
    TournamentProcessingDetailContainerComponent,
    GenerateReportsDialogComponent,
    VerifyMembershipsComponent,
    VerifyMembershipsContainerComponent
  ],
    imports: [
        CommonModule,
        MatListModule,
        MatTableModule,
        MatPaginatorModule,
        MatSortModule,
        MatToolbarModule,
        MatInputModule,
        FormsModule,
        MatIconModule,
        FlexLayoutModule,
        MatButtonModule,
        MatCardModule,
        MatDialogModule,
        MatCheckboxModule,
        TournamentProcessingRoutingModule,
        AccountModule
    ]
})
export class TournamentProcessingModule {
}
