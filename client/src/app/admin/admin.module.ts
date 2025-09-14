import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {MatCardModule} from '@angular/material/card';
import {MatButtonModule} from '@angular/material/button';
import {MatTableModule} from '@angular/material/table';
import {MatPaginatorModule} from '@angular/material/paginator';
import {MatSortModule} from '@angular/material/sort';
import {MatListModule} from '@angular/material/list';
import {MatInputModule} from '@angular/material/input';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatIconModule} from '@angular/material/icon';
import {MatSelectModule} from '@angular/material/select';
import {MatDialogModule} from '@angular/material/dialog';
import {FlexLayoutModule} from 'ng-flex-layout';
import {FormsModule} from '@angular/forms';
import {MatCheckboxModule} from '@angular/material/checkbox';

import {UserListComponent} from './user-list/user-list.component';
import {SharedModule} from '../shared/shared.module';
import {OktaUserStatusPipe} from './okta-user-status.pipe';
import {AdminRoutingModule} from './admin-routing.module';
import {AdminRatingsComponent} from './admin-ratings/admin-ratings.component';
import {AdminRatingsContainerComponent} from './admin-ratings/admin-ratings-container.component';
import {RolesDialogComponent} from './groups-dialog/roles-dialog.component';

@NgModule({
  declarations: [
    AdminRatingsComponent,
    AdminRatingsContainerComponent,
    UserListComponent,
    OktaUserStatusPipe,
    RolesDialogComponent
  ],
  imports: [
    CommonModule,
    AdminRoutingModule,
    SharedModule,
    MatCardModule,
    MatButtonModule,
    FlexLayoutModule,
    MatListModule,
    MatInputModule,
    MatTooltipModule,
    MatToolbarModule,
    FormsModule,
    MatIconModule,
    MatSelectModule,
    MatDialogModule,
    MatCheckboxModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule
  ]
})
export class AdminModule {
}
