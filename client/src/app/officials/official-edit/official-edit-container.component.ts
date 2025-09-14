import {Component} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';

import {Observable, of, Subscription} from 'rxjs';
import {first, map, switchMap} from 'rxjs/operators';
import {createSelector} from '@ngrx/store';

import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';

import {ProfileService} from '../../profile/profile.service';
import {Profile} from '../../profile/profile';
import {Official} from '../model/official.model';
import {OfficialService} from '../service/official.service';

@Component({
    selector: 'app-official-edit-container',
    template: `
    <app-official-edit [official]="official$ | async"
                       (save)="onSave($event)"
    (cancel)="onCancel($event)">
    </app-official-edit>
  `,
    styles: [],
    standalone: false
})
export class OfficialEditContainerComponent {

  official$: Observable<Official>;
  private loading$: Observable<boolean>;
  private subscriptions: Subscription = new Subscription();

  private creating: boolean;
  private officialId: number = 0;
  private profileId: string;

  constructor(private officialService: OfficialService,
              private linearProgressBarService: LinearProgressBarService,
              private profileService: ProfileService,
              private router: Router,
              private activatedRoute: ActivatedRoute) {
    const routePath = this.activatedRoute.snapshot.routeConfig.path;
    this.creating = (routePath.indexOf('create') !== -1);
    const strOfficialId = this.activatedRoute.snapshot.params['officialId'] || '0';
    this.officialId = Number(strOfficialId);
    this.profileId = this.activatedRoute.snapshot.params['profileId'] || '0';
    this.setupProgressIndicator();
    if (this.creating) {
      this.createFromProfile();
    } else {
      this.loadOfficial();
    }
  }

  private setupProgressIndicator() {
    this.loading$ = this.officialService.store.select(this.officialService.selectors.selectLoading);

    const loadingSubscription = this.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
  }

  private loadOfficial() {
    if (this.officialId !== 0) {
      const selector = createSelector(
        this.officialService.selectors.selectEntityMap,
        (entityMap) => {
          return entityMap[this.officialId];
        });

      this.official$ = this.officialService.store.select(selector);
      const subscription = this.official$.subscribe((official: Official) => {
        if (!official) {
          this.officialService.getByKey(this.officialId);
        } else {
          const officialToEdit = JSON.parse(JSON.stringify(official));
          this.official$ = of(officialToEdit);
        }
      });

      this.subscriptions.add(subscription);
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }


  onSave(official: Official) {
    this.officialService.update(official)
      .pipe(first())
      .subscribe(
        (savedOfficial: Official) => {
          // console.log('saved official', official);
          this.router.navigateByUrl('/ui/officials');
        },
        (error: any) => {
          console.error(error);
        }
      );
  }

  private createFromProfile() {
    const subscription = this.profileService.getProfile(this.profileId)
      .pipe(
        first(),
        map((profile: Profile) => {
          // console.log('got profile', profile);
          if (profile != null) {
            let official: Official = {
              id: null,
              firstName: profile.firstName,
              lastName: profile.lastName,
              profileId: profile.userId,
              refereeNumber: null,
              refereeRank: null,
              umpireNumber: null,
              umpireRank: null,
              wheelchair: null,
              membershipId: profile.membershipId,
              state: profile.state
            };
            // console.log('creating official from profile', official);
            this.officialService.upsert(official)
              .pipe(
                first(),
              ).subscribe(
              (official: Official) => {
                // console.log('Got official', official);
                const officialToEdit = JSON.parse(JSON.stringify(official));
                this.official$ = of(officialToEdit);
              }, (error) => {
                console.error(error);
              }
            );
          }
        })).subscribe();

    this.subscriptions.add(subscription);
  }

  onCancel($event: any) {
    const url = `/ui/officials`;
    this.router.navigateByUrl(url);
  }
}
