import {Component, OnDestroy} from '@angular/core';
import {createSelector} from '@ngrx/store';
import {SanctionRequest, SanctionRequestStatus} from '../model/sanction-request.model';
import {Subscription} from 'rxjs';
import * as moment from 'moment';
import {ActivatedRoute, Router} from '@angular/router';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {SanctionRequestService} from '../service/sanction-request.service';
import {AuthenticationService} from '../../user/authentication.service';
import {ErrorMessagePopupService} from '../../shared/error-message-dialog/error-message-popup.service';
import {first} from 'rxjs/operators';

@Component({
  selector: 'app-sanction-request-create',
  template: ``,
  styles: []
})
export class SanctionRequestCreateComponent implements OnDestroy {

  private subscriptions: Subscription = new Subscription();

  constructor(private activatedRoute: ActivatedRoute,
              private router: Router,
              private linearProgressBarService: LinearProgressBarService,
              private sanctionRequestService: SanctionRequestService,
              private authenticationService: AuthenticationService,
              private errorMessagePopupService: ErrorMessagePopupService
  ) {
    const strId = this.activatedRoute.snapshot.params['id'] || 0;
    const sanctionRequestId = Number(strId);
    this.setupProgressIndicator();
    this.createSanctionRequest(sanctionRequestId);
  }

  private setupProgressIndicator() {
    const loadingSubscription = this.sanctionRequestService.store.select(
      this.sanctionRequestService.selectors.selectLoading)
      .subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
  }

  private createSanctionRequest(sanctionRequestId: number) {
    // cloning from existing
    if (sanctionRequestId !== 0) {
      const selector = createSelector(
        this.sanctionRequestService.selectors.selectEntityMap,
        (entityMap) => {
          return entityMap[sanctionRequestId];
        });

      const sanctionRequest$ = this.sanctionRequestService.store.select(selector);
      const subscription = sanctionRequest$.subscribe({
        next: (sanctionRequest: SanctionRequest) => {
          if (!sanctionRequest) {
            this.sanctionRequestService.getByKey(sanctionRequestId);
          } else {
            // if making a copy of existing one change some values
            // save this copy and navigate to edit screen
            this.createNewOrClone(sanctionRequest);
          }
        }, error: (error) => {
          this.errorMessagePopupService.showError(error);
        }
      });

      this.subscriptions.add(subscription);
    } else {
      // creating completely new request, save and navigate to edit screen
      this.createNewOrClone(null);
    }
  }

  /**
   *
   * @param sanctionRequest
   * @private
   */
  private createNewOrClone(sanctionRequest: SanctionRequest) {
    let sanctionRequestToEdit: SanctionRequest = null;
    if (sanctionRequest == null) {
      sanctionRequestToEdit = new SanctionRequest();
    } else {
      sanctionRequestToEdit = JSON.parse(JSON.stringify(sanctionRequest));

    }
    sanctionRequestToEdit.id = null;
    sanctionRequestToEdit.status = SanctionRequestStatus.New;
    sanctionRequestToEdit.requestDate = new Date();
    sanctionRequestToEdit.blankEntryFormUrl = null;
    sanctionRequestToEdit.approvalRejectionNotes = null;
    sanctionRequestToEdit.tournamentName = (sanctionRequestToEdit.tournamentName != '') ?
      sanctionRequestToEdit.tournamentName + ' Copy' : 'My tournament';

    if (sanctionRequestToEdit.startDate == null) {
      sanctionRequestToEdit.startDate = new Date();
    }
    if (sanctionRequestToEdit.endDate == null) {
      sanctionRequestToEdit.endDate = new Date();
    }

    const startDate = moment(sanctionRequestToEdit.startDate);
    const endDate = moment(sanctionRequestToEdit.endDate);
    const diffDays = endDate.diff(startDate, 'days');

    // move to next year or 4 months into future if new tournament
    let proposedStartDate = (sanctionRequest == null)
      ? moment(sanctionRequestToEdit.startDate).add(4, 'months')
      : moment(sanctionRequestToEdit.startDate).add(1, 'years');
    const weekday = proposedStartDate.get('weekday');
    const addDays = 6 - weekday;  // 6 is saturday
    proposedStartDate = (addDays < 0) ? proposedStartDate.subtract(addDays, 'days') :
      ((addDays > 0) ? proposedStartDate.add(addDays, 'days')
        : proposedStartDate);

    // same number of days long.
    const proposedEndDate = moment(proposedStartDate).add(diffDays, 'days');
    const newStartDate = proposedStartDate.toDate();
    const newEndDate = proposedEndDate.toDate();

    sanctionRequestToEdit.startDate = newStartDate;
    sanctionRequestToEdit.endDate = newEndDate;
    sanctionRequestToEdit.alternateStartDate = newStartDate;
    sanctionRequestToEdit.alternateEndDate = newEndDate;
    sanctionRequestToEdit.preparerProfileId = this.authenticationService.getCurrentUserProfileId();
    sanctionRequestToEdit.coordinatorRegion = '';

    // save a copy so we have an id for storing blank entry form in repository
    this.saveAndNavigateToEdit(sanctionRequestToEdit);
  }

  private saveAndNavigateToEdit(sanctionRequest: SanctionRequest) {
    this.sanctionRequestService.upsert(sanctionRequest)
      .pipe(first())
      .subscribe(
        (savedSanctionRequest: SanctionRequest) => {
          this.router.navigateByUrl(`/ui/sanction/edit/${savedSanctionRequest.id}`);
        }, (error: any) => {
          console.error(error);
          this.errorMessagePopupService.showError(error.message);
        });
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }
}
