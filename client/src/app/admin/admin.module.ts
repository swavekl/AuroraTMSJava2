import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AdminRoutingModule } from './admin-routing.module';
import { AdminRatingsComponent } from './admin-ratings/admin-ratings.component';
import { AdminRatingsContainerComponent } from './admin-ratings/admin-ratings-container.component';
import {SharedModule} from '../shared/shared.module';
import {MatCardModule} from '@angular/material/card';
import {MatButtonModule} from '@angular/material/button';
import {FlexLayoutModule} from 'ng-flex-layout';


@NgModule({
  declarations: [
    AdminRatingsComponent,
    AdminRatingsContainerComponent
  ],
    imports: [
        CommonModule,
        AdminRoutingModule,
        SharedModule,
        MatCardModule,
        MatButtonModule,
        FlexLayoutModule
    ]
})
export class AdminModule { }
