import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FlexLayoutModule} from 'ng-flex-layout';
import {MatListModule} from '@angular/material/list';
import {MatExpansionModule} from '@angular/material/expansion';
import {MatTableModule} from '@angular/material/table';

import {PrizesRoutingModule} from './prizes-routing.module';
import {PrizeListComponent} from './prize-list/prize-list.component';
import {PrizeListContainerComponent} from './prize-list/prize-list-container.component';
import {OrdinalPipe} from './pipes/ordinal.pipe';
import {SharedModule} from '../shared/shared.module';


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
        FlexLayoutModule,
        MatExpansionModule,
        MatTableModule,
        SharedModule
    ]
})
export class PrizesModule { }
