import {Component, Input, OnInit} from '@angular/core';

/**
 * Message area which shows either an error in the error color
 * or success in green.
 */
@Component({
  selector: 'app-message-area',
  template: `
    <div [class]="isError ? 'message-errors' : 'message-success'" [style.min-height]="minHeight">{{ message }}</div>
  `,
  styleUrls: ['./message-area.component.scss']
})
export class MessageAreaComponent implements OnInit {

  @Input()
  public message: string;

  @Input()
  public isError: boolean;

  @Input()
  public minHeight: string;

  constructor() {
    this.minHeight = '54px';
    this.isError = false;
    this.message = '';
  }

  ngOnInit(): void {
  }

}
