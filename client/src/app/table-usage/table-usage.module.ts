import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { TableUsageRoutingModule } from './table-usage-routing.module';
import { TableUsageComponent } from './table-usage/table-usage.component';
import { TableUsageContainerComponent } from './table-usage/table-usage-container.component';
import {FlexModule} from '@angular/flex-layout';
import {MatListModule} from '@angular/material/list';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatSelectModule} from '@angular/material/select';
import {FormsModule} from '@angular/forms';
import {MatButtonModule} from '@angular/material/button';


@NgModule({
  declarations: [
    TableUsageComponent,
    TableUsageContainerComponent
  ],
  imports: [
    CommonModule,
    TableUsageRoutingModule,
    FlexModule,
    MatListModule,
    MatTooltipModule,
    MatToolbarModule,
    MatSelectModule,
    FormsModule,
    MatButtonModule
  ]
})
export class TableUsageModule { }
