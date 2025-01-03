import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';

import {MatListModule} from '@angular/material/list';
import {FlexLayoutModule} from 'ng-flex-layout';
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
import {ReplacePlayerPopupComponent} from './reaplace-player-popup/replace-player-popup.component';
import {MatDialogActions, MatDialogContent, MatDialogTitle} from '@angular/material/dialog';
import {MatFormField, MatLabel, MatOption, MatSelect, MatSuffix} from '@angular/material/select';
import {MatInput} from '@angular/material/input';
import {MatProgressBar} from '@angular/material/progress-bar';

@NgModule({
  declarations: [
    DrawsComponent,
    DrawsContainerComponent,
    ReplacePlayerPopupComponent
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
    DrawsCommonModule,
    MatDialogActions,
    MatDialogContent,
    MatSelect,
    MatOption,
    MatDialogTitle,
    MatFormField,
    MatLabel,
    MatInput,
    MatSuffix,
    MatProgressBar
  ]
})
export class DrawsModule {
}
