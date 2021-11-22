import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ClubAffiliationApplication} from '../model/club-affiliation-application.model';
import {StatesList} from '../../shared/states/states-list';
import {PlayingSite} from '../model/playing-site';

@Component({
  selector: 'app-club-affiliation-application',
  templateUrl: './club-affiliation-application.component.html',
  styleUrls: ['./club-affiliation-application.component.css']
})
export class ClubAffiliationApplicationComponent implements OnInit {

  @Input()
  public clubAffiliationApplication: ClubAffiliationApplication;

  @Output()
  public saved: EventEmitter<ClubAffiliationApplication> = new EventEmitter<ClubAffiliationApplication>();

  statesList: any[] = [];

  constructor() {
    this.statesList = StatesList.getCountryStatesList('US');
  }

  ngOnInit(): void {
  }

  onSave(value: any) {
    this.saved.emit(this.clubAffiliationApplication);
  }

  onCancel() {

  }

  onPayment() {

  }

  onAddPlayingSite() {
    const clone: ClubAffiliationApplication = this.clubAffiliationApplication.deepClone();
    clone.alternatePlayingSites = [...clone.alternatePlayingSites, new PlayingSite()];
    this.clubAffiliationApplication = clone;
  }
}
