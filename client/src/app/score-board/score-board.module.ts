import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {MatFormFieldModule} from '@angular/material/form-field';
import {FormsModule} from '@angular/forms';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {FlexModule} from '@angular/flex-layout';

import {SharedModule} from '../shared/shared.module';
import { ScoreBoardRoutingModule } from './score-board-routing.module';
import { ScoreBoardComponent } from './score-board/score-board.component';
import { ScoreBoardConfigureComponent } from './score-board-configure/score-board-configure.component';
import { ScoreBoardConfigureContainerComponent } from './score-board-configure/score-board-configure-container.component';
import { ScoreBoardContainerComponent } from './score-board/score-board-container.component';
import {MatGridListModule} from '@angular/material/grid-list';
import {MatButtonModule} from '@angular/material/button';


@NgModule({
  declarations: [
    ScoreBoardComponent,
    ScoreBoardConfigureComponent,
    ScoreBoardConfigureContainerComponent,
    ScoreBoardContainerComponent
  ],
  imports: [
    CommonModule,
    ScoreBoardRoutingModule,
    SharedModule,
    MatFormFieldModule,
    MatSelectModule,
    FormsModule,
    FlexModule,
    MatInputModule,
    MatGridListModule,
    MatButtonModule
  ]
})
export class ScoreBoardModule { }
