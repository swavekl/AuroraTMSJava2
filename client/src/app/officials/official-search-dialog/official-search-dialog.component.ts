import {AfterViewInit, Component, Inject, OnDestroy, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {Official} from '../model/official.model';
import {OfficialService} from '../service/official.service';
import {debounceTime, distinctUntilChanged, first, skip} from 'rxjs/operators';
import {Observable, Subscription} from 'rxjs';
import {StatesList} from '../../shared/states/states-list';
import {UntypedFormControl} from '@angular/forms';

@Component({
    selector: 'app-official-search-dialog',
    templateUrl: './official-search-dialog.component.html',
    styleUrls: ['./official-search-dialog.component.scss'],
    standalone: false
})
export class OfficialSearchDialogComponent implements OnDestroy, AfterViewInit {

  officials$: Observable<any []>;
  private subscriptions: Subscription = new Subscription();

  statesList: any[] = [];

  @ViewChild('filterNameCtrl') filterNameCtrl: UntypedFormControl;
  @ViewChild('filterStateCtrl') filterStateCtrl: UntypedFormControl;
  filterName: string;
  filterState: string;
  title: string = 'Official';

  constructor(public dialogRef: MatDialogRef<OfficialSearchDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: OfficialSearchOptions,
              private officialService: OfficialService) {
    this.statesList = StatesList.getCountryStatesList('US');
    this.title = (data?.officialType === 'referee') ? 'Referee' :
      (data?.officialType === 'umpire' ? 'Umpire' : 'Official');
  }

  ngOnDestroy() {
    this.subscriptions.unsubscribe();
  }

  ngAfterViewInit(): void {
    this.filterNameCtrl.valueChanges
      .pipe(
        skip(1),
        distinctUntilChanged(),
        debounceTime(500)
      )
      .subscribe((value) => {
        this.filterName = value;
      });
    this.filterStateCtrl.valueChanges
      .pipe(distinctUntilChanged())
      .subscribe((state) => {
        if (state != undefined) {
          this.filterState = state;
        }
      });
  }

  onSelectOfficial(selectedOfficial: Official) {
    this.dialogRef.close({action: 'ok', official: selectedOfficial});
  }

  onCancel() {
    this.dialogRef.close({action: 'cancel'});
  }

  performSearch() {
    let query = '?';
    if (this.filterName != null && this.filterName != '') {
      query += `nameContains=${this.filterName}`;
    }
    if (this.filterState != null && this.filterState != '') {
      query += (query.length > 1) ? '&' : '';
      query += `state=${this.filterState}`;
    }
    this.officials$ = this.officialService.getWithQuery(query);
    const subscription = this.officials$.pipe(first())
      .subscribe((officials) => {
        },
        (error) => {
        console.error(error);
      });
    this.subscriptions.add(subscription);
  }
}


export interface OfficialSearchOptions {
  officialType: string;
}

