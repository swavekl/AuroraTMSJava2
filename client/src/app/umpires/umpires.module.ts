import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {MatToolbar} from '@angular/material/toolbar';
import {MatButton, MatIconButton} from '@angular/material/button';

import {FlexLayoutModule} from 'ng-flex-layout';
import {
  MatCell,
  MatCellDef,
  MatColumnDef,
  MatHeaderCell,
  MatHeaderRow,
  MatHeaderRowDef,
  MatRow,
  MatRowDef,
  MatTable,
  MatTableModule
} from '@angular/material/table';
import {MatIcon} from '@angular/material/icon';
import {MatListItemIcon} from '@angular/material/list';
import {MatPaginator, MatPaginatorModule} from '@angular/material/paginator';
import {MatSort, MatSortHeader, MatSortModule} from '@angular/material/sort';
import {MatTooltip} from '@angular/material/tooltip';
import {MatDialogModule} from '@angular/material/dialog';

import {UmpiresRoutingModule} from './umpires-routing.module';
import {UmpireManagementComponent} from './umpire-management/umpire-management.component';
import {UmpireManagementContainerComponent} from './umpire-management/umpire-management-container.component';
import {UmpireSummaryTableComponent} from './umpire-management/umpire-summary-table/umpire-summary-table.component';
import {AssignUmpiresDialogComponent} from './assign-umpires-dialog/assign-umpires-dialog.component';
import {MatError, MatFormField, MatLabel, MatOption, MatSelect} from '@angular/material/select';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {SharedModule} from '../shared/shared.module';


@NgModule({
  declarations: [
    UmpireManagementComponent,
    UmpireManagementContainerComponent,
    UmpireSummaryTableComponent,
    AssignUmpiresDialogComponent
  ],
    imports: [
        CommonModule,
        UmpiresRoutingModule,
        MatToolbar,
        MatButton,
        FlexLayoutModule,
        MatCell,
        MatCellDef,
        MatColumnDef,
        MatHeaderCell,
        MatHeaderRow,
        MatHeaderRowDef,
        MatIcon,
        MatIconButton,
        MatListItemIcon,
        MatPaginator,
        MatRow,
        MatRowDef,
        MatSort,
        MatSortHeader,
        MatTable,
        MatTooltip,
        MatTableModule,
        MatPaginatorModule,
        MatSortModule,
        MatDialogModule,
        MatSelect,
        MatError,
        MatFormField,
        MatLabel,
        MatOption,
        ReactiveFormsModule,
        FormsModule,
        SharedModule
    ]
})
export class UmpiresModule {
}
