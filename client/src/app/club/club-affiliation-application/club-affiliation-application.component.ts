import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ClubAffiliationApplication} from '../model/club-affiliation-application.model';
import {StatesList} from '../../shared/states/states-list';
import {PlayingSite} from '../model/playing-site';
import {Router} from '@angular/router';

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

  constructor(private router: Router) {
    this.statesList = StatesList.getCountryStatesList('US');
  }

  ngOnInit(): void {
  }

  onSave() {
    this.saved.emit(this.clubAffiliationApplication);
  }

  onCancel() {
    this.router.navigateByUrl('/club/affiliationlist');
  }

  onPayment() {

  }

  onAddPlayingSite() {
    const clone: ClubAffiliationApplication = JSON.parse(JSON.stringify(this.clubAffiliationApplication));
    const alternatePlayingSites: PlayingSite [] = clone.alternatePlayingSites || [];
    alternatePlayingSites.push(new PlayingSite());
    clone.alternatePlayingSites = alternatePlayingSites;
    this.clubAffiliationApplication = clone;
  }

  onRemovePlayingSite(playingSiteIndex: number) {
    const clone: ClubAffiliationApplication = JSON.parse(JSON.stringify(this.clubAffiliationApplication));
    const alternatePlayingSites: PlayingSite [] = clone.alternatePlayingSites || [];
    alternatePlayingSites.splice(playingSiteIndex, 1);
    clone.alternatePlayingSites = alternatePlayingSites;
    this.clubAffiliationApplication = clone;
  }
}
