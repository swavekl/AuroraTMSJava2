import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';

import {MatTableModule} from '@angular/material/table';
import {MatPaginatorModule} from '@angular/material/paginator';
import {MatSortModule} from '@angular/material/sort';
import {MatButtonModule} from '@angular/material/button';
import {MatListModule} from '@angular/material/list';
import {MatInputModule} from '@angular/material/input';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatIconModule} from '@angular/material/icon';
import {MatSelectModule} from '@angular/material/select';
import {FlexLayoutModule} from '@angular/flex-layout';

import {OfficialsRoutingModule} from './officials-routing.module';
import {OfficialsListComponent} from './officials-list/officials-list.component';
import {OfficialEditComponent} from './official-edit/official-edit.component';
import {OfficialEditContainerComponent} from './official-edit/official-edit-container.component';
import {UmpireRankPipe} from './pipes/umpire-rank.pipe';
import {RefereeRankPipe} from './pipes/referee-rank.pipe';


@NgModule({
  declarations: [
    OfficialsListComponent,
    OfficialEditComponent,
    OfficialEditContainerComponent,
    UmpireRankPipe,
    RefereeRankPipe
  ],
  imports: [
    CommonModule,
    OfficialsRoutingModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    FlexLayoutModule,
    MatButtonModule,
    MatListModule,
    MatInputModule,
    MatTooltipModule,
    MatToolbarModule,
    FormsModule,
    MatIconModule,
    MatSelectModule
  ],
  exports: [
    UmpireRankPipe,
    RefereeRankPipe
  ]
})
export class OfficialsModule {
}
