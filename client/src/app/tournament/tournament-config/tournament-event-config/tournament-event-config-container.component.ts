import {Component, OnDestroy, OnInit} from '@angular/core';
import {Observable, of, Subscription} from 'rxjs';
import {TournamentEvent} from '../tournament-event.model';
import {ActivatedRoute, Router} from '@angular/router';
import {TournamentEventConfigService} from '../tournament-event-config.service';
import {createSelector} from '@ngrx/store';
import {first} from 'rxjs/operators';

@Component({
  selector: 'app-tournament-event-config-container',
  template: `
    <app-tournament-event-config [tournamentEvent]="tournamentEvent$ | async"
                                 (saved)="onSave($event)"
                                 (canceled)="onCancel($event)">
    </app-tournament-event-config>
  `,
  styles: []
})
export class TournamentEventConfigContainerComponent implements OnInit, OnDestroy {

  tournamentEvent$: Observable<TournamentEvent>;

  private subscriptions: Subscription = new Subscription();

  // if true we are creating new event instead of editing
  private creating: boolean;
  private tournamentId: number;

  constructor(private activatedRoute: ActivatedRoute,
              private router: Router,
              private tournamentEventConfigService: TournamentEventConfigService) {
    const routePath = this.activatedRoute.snapshot.routeConfig.path;
    this.creating = (routePath.endsWith('create'));
    const strTournamentId = this.activatedRoute.snapshot.paramMap.get('tournamentId');
    this.tournamentId = Number(strTournamentId);
    if (!this.creating) {
      const strEditedId = this.activatedRoute.snapshot.paramMap.get('id');
      const editedId = Number(strEditedId);
      const entityMapSelector = this.tournamentEventConfigService.selectors.selectEntityMap;
      const selectedEventSelector = createSelector(
        entityMapSelector,
        (entityMap) => {
          return entityMap[editedId];
        });

      // use selector on the store and once it is activated
      const selectedEvent$ = this.tournamentEventConfigService.store.select(selectedEventSelector);
      selectedEvent$.pipe(first()).subscribe((event: TournamentEvent) => {
        if (event == null) {
            this.tournamentEvent$ = tournamentEventConfigService.getByKey(this.tournamentId, editedId);
        } else {
          this.tournamentEvent$ = of (event);
        }
      });
    } else {
      const selectedEvent = history.state.data;
      const newTournamentEvent = TournamentEvent.fromDefaults(this.tournamentId, selectedEvent);
      this.tournamentEvent$ = of(newTournamentEvent);
    }
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  onSave(tournamentEvent: TournamentEvent) {
    const subscription = this.tournamentEventConfigService.upsert(tournamentEvent)
      .subscribe(
        next => {
          console.log('upserted successfully event', tournamentEvent.id);
          this.navigateBack();
        },
        error => console.log('error saving ' + error)
      );
    this.subscriptions.add(subscription);
  }

  onCancel($event: String) {
    this.navigateBack();
  }

  navigateBack() {
    this.router.navigateByUrl(`/ui/tournament/edit/${this.tournamentId}?activateTab=2`);
  }

}
