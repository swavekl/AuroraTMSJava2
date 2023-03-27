import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';

import {MatListModule} from '@angular/material/list';
import {FlexLayoutModule} from '@angular/flex-layout';
import {MatGridListModule} from '@angular/material/grid-list';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {DragDropModule} from '@angular/cdk/drag-drop';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatSlideToggleModule} from '@angular/material/slide-toggle';

import {SharedModule} from '../../shared/shared.module';
import {DrawsCommonModule} from '../draws-common/draws-common.module';
import {DrawsRoutingModule} from './draws-routing.module';
import {DrawsComponent} from './draws/draws.component';
import {DrawsContainerComponent} from './draws/draws-container.component';

@NgModule({
  declarations: [
    DrawsComponent,
    DrawsContainerComponent
  ],
  exports: [],
  imports: [
    CommonModule,
    DrawsRoutingModule,
    MatListModule,
    FlexLayoutModule,
    MatGridListModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    SharedModule,
    DragDropModule,
    MatTooltipModule,
    FormsModule,
    MatSlideToggleModule,
    DrawsCommonModule
  ]
})
export class DrawsModule {
}
