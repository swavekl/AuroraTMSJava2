import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {InsuranceRequest} from '../model/insurance-request.model';
import {StatesList} from '../../shared/states/states-list';
import {DateUtils} from '../../shared/date-utils';

@Component({
  selector: 'app-insurance',
  templateUrl: './insurance.component.html',
  styleUrls: ['./insurance.component.scss']
})
export class InsuranceComponent implements OnInit {
  // this is what we are editing
  @Input() insuranceRequest: InsuranceRequest;

  // save and cancel
  @Output() saved = new EventEmitter();
  @Output() canceled = new EventEmitter();

  // list of states in USA
  statesList: any [];

  minStartDate = new Date();
  maxStartDate = new Date();

  minEndDate: Date;
  maxEndDate: Date;

  endDateEnabled = false;

  constructor() {
    this.statesList = StatesList.getList();
    this.minStartDate.setDate(this.minStartDate.getDate() + 30);
    this.maxStartDate.setDate(this.maxStartDate.getDate() + 365);
  }

  ngOnInit(): void {
  }

  onEnableEndDate(event: any) {
    const date: Date = event.target.value;
    this.endDateEnabled = true;
    this.minEndDate = new Date(this.insuranceRequest.eventStartDate.getTime());
    this.minEndDate.setDate(this.minEndDate.getDate() + 1);
    this.maxEndDate = new Date(this.insuranceRequest.eventStartDate.getTime());
    this.maxEndDate.setDate(this.maxEndDate.getDate() + 7);
  }

  save(formValues: any) {
    const requestDate: Date = (this.insuranceRequest.requestDate != null) ? new Date (this.insuranceRequest.requestDate) : new Date();
    const dateUtils = new DateUtils();
    this.insuranceRequest.eventStartDate = dateUtils.convertFromLocalToUTCDate (this.insuranceRequest.eventStartDate);
    this.insuranceRequest.eventEndDate = dateUtils.convertFromLocalToUTCDate(this.insuranceRequest.eventEndDate);
    this.insuranceRequest.requestDate = dateUtils.convertFromLocalToUTCDate (requestDate);
    console.log('saving insurance request', this.insuranceRequest);
    this.saved.emit (this.insuranceRequest);
  }

  onCancel () {
    this.canceled.emit('cancelled');
  }

}
