import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {
  DefaultFlexAlignDirective,
  DefaultFlexDirective,
  DefaultLayoutAlignDirective,
  DefaultLayoutDirective,
  DefaultLayoutGapDirective, DefaultShowHideDirective, FlexModule
} from 'ng-flex-layout';
import {MatButton} from '@angular/material/button';
import {MatToolbar} from '@angular/material/toolbar';
import {PlayerContactInfo} from '../model/player-contact-info.model';
import {Router} from '@angular/router';
import {SharedModule} from '../../shared/shared.module';
import {NgForOf, NgIf} from '@angular/common';
import {MatIcon} from '@angular/material/icon';

@Component({
  selector: 'app-contact-players',
  imports: [
    DefaultFlexAlignDirective,
    DefaultFlexDirective,
    DefaultLayoutAlignDirective,
    DefaultLayoutDirective,
    DefaultLayoutGapDirective,
    DefaultShowHideDirective,
    FlexModule,
    MatButton,
    MatIcon,
    MatToolbar,
    SharedModule,
    NgForOf,
    NgIf
  ],
  templateUrl: './contact-players.component.html',
  styleUrl: './contact-players.component.scss'
})
export class ContactPlayersComponent {
  @Input() playerContactInfos!: PlayerContactInfo [];

  @Input() returnUrl!: string;

  constructor(private router: Router) {
  }

  protected onBack() {
    this.router.navigateByUrl(this.returnUrl);
  }
}
