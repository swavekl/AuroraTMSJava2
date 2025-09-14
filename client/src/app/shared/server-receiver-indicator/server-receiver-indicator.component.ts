import {Component, Input} from '@angular/core';

@Component({
    selector: 'app-server-receiver-indicator',
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
    styleUrls: ['./server-receiver-indicator.component.scss'],
    standalone: false
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
