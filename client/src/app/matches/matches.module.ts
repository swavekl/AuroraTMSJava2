import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { MatchesRoutingModule } from './matches-routing.module';
import { MatchesComponent } from './matches/matches.component';
import { MatchesContainerComponent } from './matches/matches-container.component';


@NgModule({
  declarations: [
    MatchesComponent,
    MatchesContainerComponent
  ],
  imports: [
    CommonModule,
    MatchesRoutingModule
  ]
})
export class MatchesModule { }
