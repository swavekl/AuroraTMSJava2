import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { SchedulingRoutingModule } from './scheduling-routing.module';
import { ScheduleManageComponent } from './manage/schedule-manage.component';
import { ScheduleManageContainerComponent } from './manage/schedule-manage-container.component';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatButtonModule} from '@angular/material/button';
import {MatOptionModule} from '@angular/material/core';
import {MatSelectModule} from '@angular/material/select';
import {DragDropModule} from '@angular/cdk/drag-drop';
import {GridsterModule} from 'angular-gridster2';
import {MatIconModule} from '@angular/material/icon';


@NgModule({
  declarations: [
    ScheduleManageComponent,
    ScheduleManageContainerComponent
  ],
  imports: [
    CommonModule,
    SchedulingRoutingModule,
    MatToolbarModule,
    MatButtonModule,
    MatOptionModule,
    MatSelectModule,
    DragDropModule,
    GridsterModule,
    MatIconModule
  ]
})
export class SchedulingModule { }
