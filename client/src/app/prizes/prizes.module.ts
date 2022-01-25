import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FlexModule} from '@angular/flex-layout';
import {MatListModule} from '@angular/material/list';
import {MatExpansionModule} from '@angular/material/expansion';
import {MatTableModule} from '@angular/material/table';

import {PrizesRoutingModule} from './prizes-routing.module';
import {PrizeListComponent} from './prize-list/prize-list.component';
import {PrizeListContainerComponent} from './prize-list/prize-list-container.component';
import {OrdinalPipe} from './pipes/ordinal.pipe';


@NgModule({
  declarations: [
    PrizeListComponent,
    PrizeListContainerComponent,
    OrdinalPipe
  ],
  imports: [
    CommonModule,
    PrizesRoutingModule,
    MatListModule,
    FlexModule,
    MatExpansionModule,
    MatTableModule
  ]
})
export class PrizesModule { }
