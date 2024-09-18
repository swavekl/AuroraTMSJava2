import {Component, Input} from '@angular/core';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'app-server-receiver-indicator',
  standalone: true,
  imports: [
    CommonModule
  ],
  template: `
      <div *ngIf="indicator != ''"
           class="server-receiver-indicator"
           [ngClass]="{
           'small-indicator' : small,
           'large-indicator': !small,
           'server-color' : (indicator === 'S'),
           'receiver-color': (indicator === 'R')}"
      >
        {{indicator}}
      </div>
  `,
  styleUrl: './server-receiver-indicator.component.scss'
})
export class ServerReceiverIndicatorComponent {
  // '' - don't display anything,
  // R - receiver
  // S - server
  @Input()
  public indicator: string;

  @Input()
  public small: boolean = false;


}
