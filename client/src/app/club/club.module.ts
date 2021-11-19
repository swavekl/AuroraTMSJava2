import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ClubRoutingModule } from './club-routing.module';
import { ClubEditComponent } from './club-edit/club-edit.component';
import { ClubNameValidatorDirective } from './club-name-validator.directive';
import { ClubListComponent } from './club-list/club-list.component';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatListModule} from '@angular/material/list';
import {MatInputModule} from '@angular/material/input';
import {FlexModule} from '@angular/flex-layout';
import {FormsModule} from '@angular/forms';
import {MatDialogModule} from '@angular/material/dialog';
import {MatSelectModule} from '@angular/material/select';
import { ClubAffiliationListComponent } from './club-affiliation-list/club-affiliation-list.component';


@NgModule({
    declarations: [
        ClubEditComponent,
        ClubNameValidatorDirective,
        ClubListComponent,
        ClubAffiliationListComponent
    ],
    exports: [
        ClubNameValidatorDirective
    ],
  imports: [
    CommonModule,
    ClubRoutingModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatToolbarModule,
    MatIconModule,
    MatButtonModule,
    MatListModule,
    MatInputModule,
    FlexModule,
    FormsModule,
    MatDialogModule,
    MatSelectModule
  ]
})
export class ClubModule { }
