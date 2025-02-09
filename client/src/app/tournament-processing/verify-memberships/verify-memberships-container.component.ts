import {Component, OnDestroy} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {first, tap} from 'rxjs/operators';
import {combineLatest, Observable, Subscription} from 'rxjs';
import {createSelector} from '@ngrx/store';

import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {MembershipInfoService} from '../service/membership-info.service';
import {MembershipInfo} from '../model/membership-info.model';
import {MembershipType} from '../../tournament/tournament-entry/model/tournament-entry.model';
import {Tournament} from '../../tournament/tournament-config/tournament.model';
import {TournamentConfigService} from '../../tournament/tournament-config/tournament-config.service';

@Component({
  selector: 'app-verify-memberships-container',
  template: `
    <app-verify-memberships
      [membershipInfos]="membershipInfos$ | async"
      [tournamentStartDate]="tournamentStartDate"
      [tournamentName]="tournamentName"
      [tournamentId]="tournamentId"
      [countPassAdult]="countPassAdult"
      [countPassJunior]="countPassJunior"
      [countBasic]="countBasic"
      [countPro]="countPro"
      [countLifetime]="countLifetime"
      [returnUrl]="returnUrl"
      [thisUrl]="thisUrl"
      (contactPlayers)="contactPlayers($event)"
    >
    </app-verify-memberships>
  `,
  styles: ``
})
export class VerifyMembershipsContainerComponent implements OnDestroy {

  membershipInfos$: Observable<MembershipInfo[]>
  tournamentStartDate: Date;
  tournamentName: string;
  tournamentId: number;
  countPassAdult: number;
  countPassJunior: number;
  countBasic: number;
  countPro: number;
  countLifetime: number;
  returnUrl: string;
  thisUrl: string;

  private subscriptions: Subscription = new Subscription();
  private loading$: Observable<boolean>;

  constructor(private activatedRoute: ActivatedRoute,
              private membershipInfoService: MembershipInfoService,
              private tournamentConfigService: TournamentConfigService,
              private linearProgressBarService: LinearProgressBarService) {

    const strTournamentId = this.activatedRoute.snapshot.params['tournamentId'] || 0;
    this.tournamentName = this.activatedRoute.snapshot.params['tournamentName'] || 'My Tournament';
    this.tournamentId = Number(strTournamentId);
    this.thisUrl = '/ui/processing/' + this.activatedRoute.snapshot.url.join('/');
    const defaultReturnUrl = `/ui/processing/detail/submit/${this.tournamentId}/${this.tournamentName}`;
    this.returnUrl = (history?.state?.returnUrl) ? history?.state?.returnUrl : defaultReturnUrl;

    this.setupProgressIndicator();
    this.loadTournamentStartDate(this.tournamentId);
    this.loadMembershipInformation(this.tournamentId);
  }

  private setupProgressIndicator() {
    this.loading$ = combineLatest(
      this.membershipInfoService.loading$,
      this.tournamentConfigService.store.select(this.tournamentConfigService.selectors.selectLoading),
      (membershipInfosLoading: boolean, tournamentLoading: boolean) => {
        return membershipInfosLoading || tournamentLoading;
      }
    );

    const loadingSubscription = this.loading$.subscribe((loading: boolean) => {
      this.linearProgressBarService.setLoading(loading);
    });
    this.subscriptions.add(loadingSubscription);
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  private loadMembershipInformation(tournamentId: number) {
    this.membershipInfos$ = this.membershipInfoService.load(tournamentId)
      .pipe(tap({
        next: (membershipInfos:  MembershipInfo []) => {
          let countPassAdult: number = 0;
          let countPassJunior: number = 0;
          let countBasic: number = 0;
          let countPro: number = 0;
          let countLifetime: number = 0;
          for (let i = 0; i < membershipInfos.length; i++) {
            const membershipInfo: MembershipInfo = membershipInfos[i];
            if (membershipInfo.membershipType === MembershipType.TOURNAMENT_PASS_ADULT) {
              countPassAdult++;
            }
            if (membershipInfo.membershipType === MembershipType.TOURNAMENT_PASS_JUNIOR) {
              countPassJunior++;
            }
            if (membershipInfo.membershipType === MembershipType.BASIC_PLAN) {
              countBasic++;
            }
            if (membershipInfo.membershipType === MembershipType.PRO_PLAN) {
              countPro++;
            }
            if (membershipInfo.membershipType === MembershipType.LIFETIME) {
              countLifetime++;
            }
          }
          this.countPassAdult = countPassAdult;
          this.countPassJunior = countPassJunior;
          this.countBasic = countBasic;
          this.countPro = countPro;
          this.countLifetime = countLifetime;
        }
      }));
  }

  private loadTournamentStartDate(tournamentId: number) {
    const selectedTournamentSelector = createSelector(
      this.tournamentConfigService.selectors.selectEntityMap,
      (entityMap) => {
        return entityMap[tournamentId];
      });
    const localTournament$ = this.tournamentConfigService.store.select(selectedTournamentSelector);
    const subscription = localTournament$
      .subscribe((tournament: Tournament) => {
        if (!tournament) {
          this.tournamentConfigService.getByKey(tournamentId);
        } else {
          this.tournamentStartDate = tournament.startDate;
        }
      });
    this.subscriptions.add(subscription);
  }

  /**
   * Sends emails to
   * @param membershipInfos
   */
  contactPlayers(membershipInfos: MembershipInfo[]) {
    this.membershipInfoService.contactPlayers(this.tournamentId, membershipInfos)
      .pipe(first())
      .subscribe();
  }
}
