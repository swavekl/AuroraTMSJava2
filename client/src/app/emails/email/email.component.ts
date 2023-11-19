import {Component, EventEmitter, Input, Output} from '@angular/core';

@Component({
  selector: 'app-email',
  templateUrl: './email.component.html',
  styleUrls: ['./email.component.scss']
})
export class EmailComponent {

  @Input()
  public tournamentName: string;

  @Input()
  public emailAddresses: string;

  @Output()
  private eventEmitter: EventEmitter<any> = new EventEmitter<any>();


  getAllEmails() {
    this.eventEmitter.emit('getemails');
  }

  back() {
    this.eventEmitter.emit('back');
  }
}
