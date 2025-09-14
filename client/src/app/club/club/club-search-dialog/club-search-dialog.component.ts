import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {StatesList} from '../../../shared/states/states-list';
import {ClubService} from '../service/club.service';
import {Observable} from 'rxjs';
import {Club} from '../model/club.model';
import {ClubSearchData} from '../service/club-search-popup.service';

@Component({
    selector: 'app-club-search-dialog',
    templateUrl: './club-search-dialog.component.html',
    styleUrls: ['./club-search-dialog.component.scss'],
    standalone: false
})
export class ClubSearchDialogComponent implements OnInit {

  statesList: any [];
  state: string;

  searchNameOrAcronym: string;

  foundClubs$: Observable<Club[]> = null;

  loading$: Observable<boolean> = null;

  constructor(public dialogRef: MatDialogRef<ClubSearchDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: ClubSearchData,
              private clubService: ClubService) {
    this.state = data?.state;
    const countryCode: string = (data?.countryCode != null) ? data.countryCode : 'US';
    this.statesList = StatesList.getCountryStatesList(countryCode);
    this.loading$ = this.clubService.loading$
  }

  ngOnInit(): void {
        this.onSearchInternal(this.state, null);
    }

  onSearch() {
    this.onSearchInternal(this.state, this.searchNameOrAcronym);
  }

  onSearchInternal(state: string, nameLike: string) {
    let query: string = 'state=' + state;
    if (nameLike?.length > 0) {
      query += `&nameContains=${nameLike}`
    }
    query += '&sort=clubName,ASC';
    this.foundClubs$ = this.clubService.loadWithQuery(query);
  }

  onSelection(club: Club) {
    this.dialogRef.close({action: 'ok', selectedClub: club});
  }

  onCancel() {
    this.dialogRef.close({action: 'cancel'});
  }
}
