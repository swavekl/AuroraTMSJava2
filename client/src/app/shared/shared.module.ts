import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LinearProgressBarComponent } from './linear-progress-bar/linear-progress-bar.component';
import {MatProgressBarModule} from '@angular/material/progress-bar';



@NgModule({
  declarations: [LinearProgressBarComponent],
  exports: [
    LinearProgressBarComponent
  ],
  imports: [
    CommonModule,
    MatProgressBarModule
  ]
})
export class SharedModule { }
