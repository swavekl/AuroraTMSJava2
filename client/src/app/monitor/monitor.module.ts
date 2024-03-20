import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatButtonModule} from '@angular/material/button';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatInputModule} from '@angular/material/input';
import {FormsModule} from '@angular/forms';
import {MatSelectModule} from '@angular/material/select';
import {FlexLayoutModule} from 'ng-flex-layout';
import {SharedModule} from '../shared/shared.module';

import {MonitorRoutingModule} from './monitor-routing.module';
import {MonitorDisplayComponent} from './monitor-display/monitor-display.component';
import {MonitorConnectComponent} from './monitor-connect/monitor-connect.component';
import {MonitorConnectContainerComponent} from './monitor-connect/monitor-connect-container.component';
import {MonitorDisplayContainerComponent} from './monitor-display/monitor-display-container.component';
import {MatGridListModule} from '@angular/material/grid-list';

@NgModule({
  declarations: [
    MonitorDisplayComponent,
    MonitorConnectComponent,
    MonitorConnectContainerComponent,
    MonitorDisplayContainerComponent
  ],
    imports: [
        CommonModule,
        MonitorRoutingModule,
        MatButtonModule,
        MatToolbarModule,
        MatInputModule,
        FormsModule,
        FlexLayoutModule,
        MatSelectModule,
        SharedModule,
        MatGridListModule
    ],
  providers: [
  ]
})
export class MonitorModule { }
