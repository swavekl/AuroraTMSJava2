import {AfterViewInit, Component, EventEmitter, Input, OnDestroy, Output, ViewChild} from '@angular/core';

import {Subscription} from 'rxjs';
import {debounceTime, distinctUntilChanged, skip} from 'rxjs/operators';

import {MatTable} from '@angular/material/table';
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from '@angular/material/sort';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';

import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {ConfirmationPopupComponent} from '../../shared/confirmation-popup/confirmation-popup.component';
import {EmailCampaignListDataSource} from './email-campaign-list-datasource';
import {EmailCampaignService} from '../service/email-campaign.service';
import {EmailCampaign} from '../model/email-campaign.model';
import {EmailServerConfigDialogComponent} from '../email-server-config-dialog/email-server-config-dialog.component';
import {EmailServerConfiguration} from '../model/email-server-configuration.model';
import {UntypedFormControl} from '@angular/forms';

@Component({
  selector: 'app-email-campaign-list',
  templateUrl: './email-campaign-list.component.html',
  styleUrls: ['./email-campaign-list.component.scss']
})
export class EmailCampaignListComponent implements AfterViewInit, OnDestroy {

  @Input()
  public tournamentId: number;

  @Input()
  public tournamentName: string;

  @Input()
  public emailServerConfiguration: EmailServerConfiguration;

  @Input()
  public emailAddresses: string;

  @Output()
  private eventEmitter: EventEmitter<any> = new EventEmitter<any>();

  @Output()
  private emailConfigSave: EventEmitter<EmailServerConfiguration> = new EventEmitter<EmailServerConfiguration>();

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild(MatTable) table!: MatTable<EmailCampaign>;

  @ViewChild('filterCampaignNameCtrl') filterCampaignNameCtrl: UntypedFormControl;
  filterCampaignName: string;

  dataSource: EmailCampaignListDataSource;

  /** Columns displayed in the table. Columns IDs can be added, removed, or reordered. */
  displayColumns: string [] = ['name', 'tournamentName', 'dateSent', 'emailsCount', 'actions'];

  private subscriptions: Subscription = new Subscription();

  constructor(private linearProgressBarService: LinearProgressBarService,
              private emailCampaignService: EmailCampaignService,
              private dialog: MatDialog) {
    this.dataSource = new EmailCampaignListDataSource(emailCampaignService);
    this.setupProgressIndicator();
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  ngAfterViewInit(): void {
    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
    this.table.dataSource = this.dataSource;

    this.filterCampaignNameCtrl.valueChanges
      .pipe(
        skip(1),
        distinctUntilChanged(),
        debounceTime(500)
      )
      .subscribe((value) => {
        this.dataSource.filterByName$.next(value);
      });
  }

  clearFilter(): void {
    this.filterCampaignName = '';
  }

  private setupProgressIndicator() {
    const loadingSubscription = this.emailCampaignService.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
  }

  configureServer() {
    const config: MatDialogConfig = {
      width: '575px', height: '380px', data: this.emailServerConfiguration
    };
    const dialogRef = this.dialog.open(EmailServerConfigDialogComponent, config);
    dialogRef.afterClosed().subscribe(result => {
      if (result.action === 'ok') {
        this.onEmailConfigSave(result.config);
      } else {
        this.onEmailConfigCanceled();
      }
    });
  }

  onEmailConfigSave(config: EmailServerConfiguration) {
    this.emailConfigSave.emit(config);
  }

  onEmailConfigCanceled() {

  }

  back() {
    this.eventEmitter.emit('back');
  }

  onDelete(id, emailCampaignName) {
    const message = `Are you sure you want to delete email '${emailCampaignName}' ?`;
    const config = {
      width: '350px', height: '200px', data: {
        title: 'Warning',
        message: message, contentAreaHeight: 80, showCancel: true,
        okText: 'Delete'
      }
    };
    const dialogRef = this.dialog.open(ConfirmationPopupComponent, config);
    dialogRef.afterClosed().subscribe(result => {
      if (result === 'ok') {
        this.dataSource.delete(id);
      }
    });
  }

  isServerConfigured() {
    return this.emailServerConfiguration != null;
  }
}
