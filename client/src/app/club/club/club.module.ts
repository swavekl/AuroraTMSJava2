import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';

import {MatTableModule} from '@angular/material/table';
import {MatPaginatorModule} from '@angular/material/paginator';
import {MatSortModule} from '@angular/material/sort';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatListModule} from '@angular/material/list';
import {MatInputModule} from '@angular/material/input';
import {MatDialogModule} from '@angular/material/dialog';
import {MatSelectModule} from '@angular/material/select';

import {FlexLayoutModule} from 'ng-flex-layout';

import {ClubRoutingModule} from './club-routing.module';
import {ClubEditComponent} from './club-edit/club-edit.component';
import {ClubNameValidatorDirective} from './club-name-validator.directive';
import {ClubListComponent} from './club-list/club-list.component';
import {ClubEditPopupService} from './service/club-edit-popup.service';
import {ClubSearchDialogComponent} from './club-search-dialog/club-search-dialog.component';
import {ClubSearchPopupService} from './service/club-search-popup.service';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {MatTooltip} from '@angular/material/tooltip';


@NgModule({
  declarations: [
    ClubEditComponent,
    ClubNameValidatorDirective,
    ClubListComponent,
    ClubSearchDialogComponent
  ],
  exports: [
    ClubNameValidatorDirective
  ],
    imports: [
        CommonModule,
        FormsModule,
        ClubRoutingModule,
        MatTableModule,
        MatPaginatorModule,
        MatSortModule,
        MatToolbarModule,
        MatIconModule,
        MatButtonModule,
        MatListModule,
        MatSelectModule,
        MatInputModule,
        MatDialogModule,
        FlexLayoutModule,
        MatProgressBarModule,
        MatTooltip
    ],
  providers: [
    ClubEditPopupService,
    ClubSearchPopupService
  ]
})
export class ClubModule {
}
