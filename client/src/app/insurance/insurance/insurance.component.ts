import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {InsuranceRequest} from '../model/insurance-request.model';
import {StatesList} from '../../shared/states/states-list';
import {DateUtils} from '../../shared/date-utils';
import {InsuranceRequestStatus} from '../model/insurance-request-status';

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

  save() {
    const requestDate: Date = (this.insuranceRequest.requestDate != null) ? new Date (this.insuranceRequest.requestDate) : new Date();
    const dateUtils = new DateUtils();
    this.insuranceRequest.eventStartDate = dateUtils.convertFromLocalToUTCDate (this.insuranceRequest.eventStartDate);
    this.insuranceRequest.eventEndDate = dateUtils.convertFromLocalToUTCDate(this.insuranceRequest.eventEndDate);
    this.insuranceRequest.requestDate = dateUtils.convertFromLocalToUTCDate (requestDate);
    // console.log('saving insurance request', this.insuranceRequest);
    this.saved.emit (this.insuranceRequest);
  }

  onCancel () {
    this.canceled.emit('cancelled');
  }

  onSubmitRequest() {
    this.insuranceRequest.status = InsuranceRequestStatus.Submitted;
    this.save();
  }

  getCertificateStoragePath() {
    return (this.insuranceRequest?.id != null)
      ? `insurance_request/${this.insuranceRequest.id}/certificate`
      : null;
  }

  getAgreementStoragePath() {
    return (this.insuranceRequest?.id != null)
      ? `insurance_request/${this.insuranceRequest.id}/additional_insured`
      : null;
  }

  onCertificateUploadFinished(downloadUrl: string) {
    // https%3A%2F%2Fgateway-pc%3A4200%2Fapi%2Ffilerepository%2Fdownload%2Finsurance_request%2F4%2Fcertificate%2F2020-21+USATT+GL+Certificate+-+Fox+Valley+Park+District.pdf
    this.insuranceRequest.status = InsuranceRequestStatus.Completed;
    this.insuranceRequest.certificateUrl = downloadUrl;
    console.log('downlaodUrl', downloadUrl);
  }

  onAgreementUploadFinished(downloadUrl: string) {
    this.insuranceRequest.additionalInsuredAgreementUrl = downloadUrl;
  }
}
