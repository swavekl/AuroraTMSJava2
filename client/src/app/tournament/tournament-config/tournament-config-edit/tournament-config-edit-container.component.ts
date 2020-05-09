import {AfterViewInit, ChangeDetectionStrategy, Component, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {TournamentConfigService} from '../tournament-config.service';
import {Observable} from 'rxjs';
import {Tournament} from '../tournament.model';
import {TournamentConfigEditComponent} from './tournament-config-edit.component';

@Component({
  selector: 'app-tournament-config-edit-container',
  template: `
    <app-linear-progress-bar [loading]="loading$ | async"></app-linear-progress-bar>
    <app-tournament-config-edit [tournament]="tournament$ | async"
                                (saved)="onSave($event)"
                                (canceled)="onCancel($event)"></app-tournament-config-edit>
  `,
  styles: [],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TournamentConfigEditContainerComponent implements OnInit, AfterViewInit {

  tournament$: Observable<Tournament>;
  loading$: Observable<boolean>;
  public editedId: number;

  // child component reference for getting back to the events list tab
  @ViewChild(TournamentConfigEditComponent)
  tournamentConfigEditComponent: TournamentConfigEditComponent;

  constructor(public tournamentConfigService: TournamentConfigService,
              private activatedRoute: ActivatedRoute,
              private router: Router) {
  }

  ngOnInit(): void {
    this.editedId = this.activatedRoute.snapshot.params['id'] || 0;
    this.tournament$ = this.tournamentConfigService.getByKey(this.editedId);
    this.tournament$.subscribe(data => {
      // console.log('got tournament data ' + JSON.stringify(data));
      return data;
    });
  }

  ngAfterViewInit(): void {
    // if we are coming back from adding a new event then we want to make Events tab the active tab
    const activateTab = this.activatedRoute.snapshot.queryParams['activateTab'];
    if (this.tournamentConfigEditComponent && activateTab) {
      this.tournamentConfigEditComponent.setActiveTab(activateTab);
    }
  }

  onSave(tournament: Tournament) {
    this.tournamentConfigService.upsert(tournament, null).subscribe(
      () => this.navigateBack(),
      (err: any) => console.log('error saving ' + err)
    );
  }

  onCancel($event: any) {
    this.navigateBack();
  }

  // back to the list of tournaments
  navigateBack() {
    this.router.navigateByUrl('/tournamentsconfig');
  }

}
