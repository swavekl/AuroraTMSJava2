import {Component, OnDestroy, OnInit} from '@angular/core';
import {Observable, of, Subscription} from 'rxjs';
import {TournamentEvent} from '../tournament-event.model';
import {ActivatedRoute, Router} from '@angular/router';
import {TournamentEventConfigService} from '../tournament-event-config.service';
import {Store} from '@ngrx/store';

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

  private subscriptions: Subscription = new Subscription ();

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
      const editedId = this.activatedRoute.snapshot.paramMap.get('id');
      this.tournamentEvent$ = tournamentEventConfigService.getByKey(this.tournamentId, editedId);
    } else {
      const selectedEvent = history.state.data;
      const newTournamentEvent = TournamentEvent.fromDefaults(this.tournamentId, selectedEvent);
      this.tournamentEvent$ = of(newTournamentEvent);
    }
    const subscription = this.tournamentEvent$.subscribe(
      (event: TournamentEvent) => {
        console.log('got event to edit', event.name);
        return event;
      },
      error => {
        console.log('error loading event data', error);
      }
    );
    this.subscriptions.add(subscription);
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
          console.log ('upserted successfully event', tournamentEvent.id);
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
    this.router.navigateByUrl(`tournament/edit/${this.tournamentId}?activateTab=2`);
  }

}
