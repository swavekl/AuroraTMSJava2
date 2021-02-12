import {Component, Input, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {UsattPlayerRecord} from '../model/usatt-player-record.model';
import {UsattPlayerRecordService} from '../service/usatt-player-record.service';

@Component({
  selector: 'app-usatt-record-search',
  templateUrl: './usatt-record-search.component.html',
  styleUrls: ['./usatt-record-search.component.css']
})
export class UsattRecordSearchComponent implements OnInit {
  foundPlayers$: Observable<UsattPlayerRecord []>;
  loading$: Observable<boolean>;

  @Input()
  firstName: string;

  @Input()
  lastName: string;

  membershipId: string;

  constructor(private usattPlayerRecordService: UsattPlayerRecordService) {
    this.loading$ = this.usattPlayerRecordService.loading$;
  }

  ngOnInit(): void {
    this.foundPlayers$ = this.usattPlayerRecordService.searchByNames(this.firstName, this.lastName);
  }

  onSelection(selected: UsattPlayerRecord) {
    console.log ('selected', selected);
  }

  onSearch(formValues: any) {
    console.log ('formValues', formValues);
  }
}
