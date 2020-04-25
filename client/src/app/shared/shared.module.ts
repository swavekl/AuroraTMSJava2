import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {LinearProgressBarComponent} from './linear-progress-bar/linear-progress-bar.component';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {DateBeforeDirective} from './directives/date-before.directive';


@NgModule({
  declarations: [
    LinearProgressBarComponent,
    DateBeforeDirective
  ],
  exports: [
    LinearProgressBarComponent,
    DateBeforeDirective
  ],
  imports: [
    CommonModule,
    MatProgressBarModule
  ]
})
export class SharedModule {
}
