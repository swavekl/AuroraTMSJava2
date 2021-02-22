import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {Observable, of, Subscription} from 'rxjs';
import {UsattPlayerRecord} from '../model/usatt-player-record.model';
import {UsattPlayerRecordService} from '../service/usatt-player-record.service';
import {first} from 'rxjs/operators';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';

@Component({
  selector: 'app-usatt-record-search',
  templateUrl: './usatt-record-search.component.html',
  styleUrls: ['./usatt-record-search.component.css']
})
export class UsattRecordSearchComponent implements OnInit, OnDestroy {
  foundPlayers$: Observable<UsattPlayerRecord []>;
  loading$: Observable<boolean>;

  @Input()
  firstName: string;

  @Input()
  lastName: string;

  @Output()
  private selection: EventEmitter<UsattPlayerRecord> = new EventEmitter<UsattPlayerRecord>();

  membershipId: string;

  pageNum: number;

  public searchingByMembershipId: boolean;

  private subscriptions: Subscription = new Subscription();

  constructor(private usattPlayerRecordService: UsattPlayerRecordService,
              private linearProgressBarService: LinearProgressBarService) {
    this.loading$ = this.usattPlayerRecordService.loading$;
    this.pageNum = 0;
    this.searchingByMembershipId = true;
  }

  ngOnInit(): void {
    const subscription = this.usattPlayerRecordService.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(subscription);

    this.performSearch(this.firstName, this.lastName, null, 0);
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  onSelection(selected: UsattPlayerRecord) {
    this.selection.emit(selected);
  }

  public onPreviousPage(formValues: any) {
    if (this.pageNum > 0) {
      this.pageNum--;
    }
    this.onSearch(formValues);
  }

  public onNextPage(formValues: any) {
    this.pageNum++;
    this.onSearch(formValues);
  }

  onSearchByMembershipId (formValues: any) {
    this.searchingByMembershipId = true;
    this.onSearch(formValues);
  }

  public onSearchByNames (formValues: any) {
    this.searchingByMembershipId = false;
    this.onSearch(formValues);
  }

  private onSearch (formValues: any) {
    if (this.searchingByMembershipId) {
      this.performSearch(null, null, formValues.membershipId, this.pageNum);
    } else {
      this.performSearch(formValues.firstName, formValues.lastName, null, this.pageNum);
    }
  }

  performSearch(firstName: string, lastName: string, membershipId: number, page: number) {
    if (membershipId) {
      this.usattPlayerRecordService.getByMembershipId(membershipId)
        .pipe(first())
        .subscribe((record: UsattPlayerRecord) => {
          if (record != null) {
            // console.log('got usatt player record', record);
            const playerArray = [];
            playerArray.push(record);
            this.foundPlayers$ = of(playerArray);
          } else {
            // console.log('got usatt player record', record);
            this.foundPlayers$ = of([]);
          }
        });
    } else if (firstName || lastName) {
      this.usattPlayerRecordService.searchByNames(firstName, lastName, this.pageNum)
        .pipe(first())
        .subscribe((records: UsattPlayerRecord[]) => {
          if (records != null) {
            // console.log('got usatt player record', records);
            this.foundPlayers$ = of(records);
          } else {
            this.foundPlayers$ = of([]);
          }
        });
    }
  }
}
