import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatButtonModule} from '@angular/material/button';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatInputModule} from '@angular/material/input';
import {FormsModule} from '@angular/forms';
import {FlexModule} from '@angular/flex-layout';
import {MonitorRoutingModule} from './monitor-routing.module';
import {MonitorDisplayComponent} from './monitor-display/monitor-display.component';
import {MonitorConnectComponent} from './monitor-connect/monitor-connect.component';
import {MonitorConnectContainerComponent} from './monitor-connect/monitor-connect-container.component';
import {MonitorDisplayContainerComponent} from './monitor-display/monitor-display-container.component';

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
        FlexModule
    ],
  providers: [
  ]
})
export class MonitorModule { }
