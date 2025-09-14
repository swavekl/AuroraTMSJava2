import {Component, OnInit} from '@angular/core';
import {EventDefaults} from '../model/event-defaults';
import {MatDialogRef} from '@angular/material/dialog';

@Component({
    selector: 'app-select-event-dialog',
    templateUrl: './select-event-dialog.component.html',
    styleUrls: ['./select-event-dialog.component.css'],
    standalone: false
})
export class SelectEventDialogComponent implements OnInit {

  // selected event
  selectedEvent: number;

  eventDefaults: any [];

  constructor(public dialogRef: MatDialogRef<SelectEventDialogComponent>) {
    this.selectedEvent = 0;
    this.eventDefaults = new EventDefaults().eventDefaults;
  }

  ngOnInit(): void {
  }

  onOk(formValues: any) {
    const selectedEventDefaults = this.eventDefaults[this.selectedEvent];
    console.log('selected event defaults', selectedEventDefaults);
    this.dialogRef.close(selectedEventDefaults);
  }
}
