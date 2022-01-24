import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { PrizesRoutingModule } from './prizes-routing.module';
import { PrizeListComponent } from './prize-list/prize-list.component';
import { PrizeListContainerComponent } from './prize-list/prize-list-container.component';
import {MatListModule} from '@angular/material/list';
import {FlexModule} from '@angular/flex-layout';
import { OrdinalPipe } from './pipes/ordinal.pipe';


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
    FlexModule
  ]
})
export class PrizesModule { }
