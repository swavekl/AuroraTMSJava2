import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {DrawsRoutingModule} from './draws-routing.module';
import {DrawsComponent} from './draws/draws.component';
import {DrawsContainerComponent} from './draws/draws-container.component';
import {MatListModule} from '@angular/material/list';
import {FlexLayoutModule} from '@angular/flex-layout';
import {MatGridListModule} from '@angular/material/grid-list';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';


@NgModule({
  declarations: [
    DrawsComponent,
    DrawsContainerComponent
  ],
  imports: [
    CommonModule,
    DrawsRoutingModule,
    MatListModule,
    FlexLayoutModule,
    MatGridListModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule
  ]
})
export class DrawsModule {
}
