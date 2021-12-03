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
    // copy changed values into this new object
    let insuranceRequestToSave: InsuranceRequest = new InsuranceRequest();
    insuranceRequestToSave = Object.assign (insuranceRequestToSave, formValues);
    const requestDate: Date = (formValues.requestDate != null) ? new Date (formValues.requestDate) : new Date();
    const dateUtils = new DateUtils();
    insuranceRequestToSave.eventStartDate = dateUtils.convertFromLocalToUTCDate (formValues.eventStartDate);
    insuranceRequestToSave.eventEndDate = dateUtils.convertFromLocalToUTCDate(formValues.eventEndDate);
    insuranceRequestToSave.requestDate = dateUtils.convertFromLocalToUTCDate (requestDate);
    this.saved.emit (insuranceRequestToSave);
  }

  onCancel () {
    this.canceled.emit('cancelled');
  }

}
