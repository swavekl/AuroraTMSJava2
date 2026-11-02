import {Component, OnDestroy} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {AsyncPipe} from '@angular/common';
import {combineLatest, Observable, of, Subscription} from 'rxjs';
import {map, switchMap, take} from 'rxjs/operators';
import {createSelector} from '@ngrx/store';
import {LinearProgressBarService} from '../../shared/linear-progress-bar/linear-progress-bar.service';
import {ContactPlayersComponent} from './contact-players.component';
import {PlayerContactInfo} from '../model/player-contact-info.model';
import {MatchCardService} from '../../matches/service/match-card.service';
import {MatchCard} from '../../matches/model/match-card.model';
import {PlayerContactInfoService} from '../service/player-contact-info.service';

@Component({
  selector: 'app-contact-players-container',
  imports: [
    ContactPlayersComponent,
    AsyncPipe
  ],
  template: `
    <app-contact-players
      [playerContactInfos]="playerContactInfos$ | async"
      [returnUrl]="returnUrl"
    >
    </app-contact-players>
  `,
  styles: ``
})
export class ContactPlayersContainerComponent implements OnDestroy {

  // This container supplies the presenter with ready-to-display rows; the presenter
  // does not load match cards or phone numbers itself.
  public playerContactInfos$: Observable<PlayerContactInfo[]> = of([]);
  public returnUrl: string;

  private subscriptions: Subscription = new Subscription();

  constructor(private activatedRoute: ActivatedRoute,
              private linearProgressBarService: LinearProgressBarService,
              private matchCardService: MatchCardService,
              private playerContactInfoService: PlayerContactInfoService) {
    const matchCardId = Number(this.activatedRoute.snapshot.params['matchCardId'] || 0);
    this.returnUrl = history.state?.returnUrl || '/ui/today';

    this.setupProgressIndicator();
    this.loadPlayerContactInfo(matchCardId);
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  private setupProgressIndicator(): void {
    // Show the shared progress bar while either the match card or contact numbers load.
    const loading$ = combineLatest([
      this.matchCardService.loading$,
      this.playerContactInfoService.loading$
    ]).pipe(map(([matchCardLoading, contactInfoLoading]) => matchCardLoading || contactInfoLoading));
    this.subscriptions.add(loading$.subscribe(loading => this.linearProgressBarService.setLoading(loading)));
  }

  private loadPlayerContactInfo(matchCardId: number): void {
    if (!matchCardId) {
      return;
    }

    // Prefer the NgRx-cached card. If it has no player-name map, fetch a full card
    // so the phone-number response can remain limited to profile-id-to-phone data.
    const matchCardSelector = createSelector(
      this.matchCardService.selectors.selectEntityMap,
      entityMap => entityMap[matchCardId]
    );
    const matchCardSubscription = this.matchCardService.store.select(matchCardSelector).pipe(
      take(1),
      switchMap(cachedMatchCard => {
        const cachedNames = cachedMatchCard?.profileIdToNameMap;
        if (cachedMatchCard && cachedNames && Object.keys(cachedNames).length > 0) {
          return of(cachedMatchCard);
        }
        return this.matchCardService.getByKey(matchCardId);
      }),
      switchMap((matchCard: MatchCard) => {
        // Doubles sides encode both partner profile IDs in one semicolon-separated value.
        const profileIds = this.getUniqueProfileIds(matchCard);
        return profileIds.length > 0
          ? this.playerContactInfoService.getForMatchCard(matchCardId, profileIds).pipe(
            map(phoneNumbers => this.toPlayerContactInfos(matchCard, phoneNumbers)))
          : of([]);
      })
    ).subscribe({
      next: playerContactInfos => {
        this.playerContactInfos$ = of(playerContactInfos);
      },
      error: () => this.playerContactInfos$ = of([])
    });
    this.subscriptions.add(matchCardSubscription);
  }

  private getUniqueProfileIds(matchCard: MatchCard): string[] {
    const profileIds = new Set<string>();
    (matchCard.matches || []).forEach(match => {
      [match.playerAProfileId, match.playerBProfileId].forEach(teamProfileIds => {
        (teamProfileIds || '').split(';').forEach(profileId => {
          const normalizedProfileId = profileId.trim();
          if (normalizedProfileId && normalizedProfileId !== 'TBD') {
            profileIds.add(normalizedProfileId);
          }
        });
      });
    });
    return Array.from(profileIds);
  }

  private toPlayerContactInfos(matchCard: MatchCard,
                               phoneNumbers: Record<string, string | null>): PlayerContactInfo[] {
    // Match-card names are keyed by a singles profile ID or a doubles team ID.
    // Expand each doubles name back into its two individual contact rows.
    const profileIdToName = new Map<string, string>();
    Object.entries(matchCard.profileIdToNameMap || {})
      .forEach(([teamProfileIds, teamPlayerNames]) => {
        const profileIds = teamProfileIds.split(';');
        const strTeamPlayerNames: string = (teamPlayerNames || '') as string;
        const playerNames = strTeamPlayerNames.split(' / ');
        profileIds.forEach((profileId, index) => {
          profileIdToName.set(profileId, playerNames[index] || profileId);
        });
      });

    // The server returns only profile ID -> phone number. Join that map to the
    // cached names and the schedule order to create the presenter's row objects.
    const profileOrder = this.getProfileOrder(matchCard);
    // A null number is intentional for the signed-in player's doubles row: it
    // keeps the pair visible while the template hides that row's call icon.
    return Object.entries(phoneNumbers)
      .map(([profileId, mobilePhone]) => {
        const order = profileOrder.get(profileId);
        return {
          profileId,
          playerFullName: profileIdToName.get(profileId) || profileId,
          mobilePhone,
          position: order?.position ?? Number.MAX_SAFE_INTEGER,
          teamId: order?.teamId
        };
      })
      .sort((first, second) => first.position - second.position);
  }

  private getProfileOrder(matchCard: MatchCard): Map<string, {position: number; teamId?: string}> {
    const profileOrder = new Map<string, {position: number; teamId?: string}>();
    const teams = new Map<string, {
      profileIds: string[];
      teamId?: string;
      playerCode: string;
      insertionOrder: number;
    }>();
    let nextPosition = 0;
    let nextTeamOrder = 0;

    // Add each side once. For doubles, retain the member order from the encoded
    // side while using a canonical key to recognize the same team in later matches.
    const addTeam = (encodedProfileIds: string, playerCode: string): void => {
      const teamProfileIds = (encodedProfileIds || '').split(';')
        .map(profileId => profileId.trim())
        .filter(profileId => profileId && profileId !== 'TBD');
      if (teamProfileIds.length === 0) {
        return;
      }

      const canonicalTeamId = [...teamProfileIds].sort().join(';');
      if (!teams.has(canonicalTeamId)) {
        teams.set(canonicalTeamId, {
          profileIds: teamProfileIds,
          teamId: teamProfileIds.length > 1 ? canonicalTeamId : undefined,
          playerCode: playerCode ? String(playerCode) : '\uffff',
          insertionOrder: nextTeamOrder++
        });
      }
    };

    // PlayerScheduleService sorts details by player code (A, B, C...). Both
    // members of a doubles side share a code, so sorting these side groups by
    // that code reproduces the schedule order and keeps partners adjacent.
    (matchCard.matches || []).forEach(match => {
      addTeam(match.playerAProfileId, match.playerALetter);
      addTeam(match.playerBProfileId, match.playerBLetter);
    });

    // Preserve first-seen match order when player codes tie, then assign a
    // consecutive display position to each partner in the side.
    Array.from(teams.values())
      .sort((first, second) => first.playerCode === second.playerCode
        ? first.insertionOrder - second.insertionOrder
        : first.playerCode < second.playerCode ? -1 : 1)
      .forEach(team => team.profileIds.forEach(profileId => {
        if (!profileOrder.has(profileId)) {
          profileOrder.set(profileId, {
            position: nextPosition++,
            teamId: team.teamId
          });
        }
      }));

    // Include any profile that did not have a player code on a match side, placing
    // those uncommon entries after the normally ordered players.
    this.getUniqueProfileIds(matchCard).forEach(profileId => {
      if (!profileOrder.has(profileId)) {
        profileOrder.set(profileId, {position: nextPosition++});
      }
    });

    return profileOrder;
  }
}
