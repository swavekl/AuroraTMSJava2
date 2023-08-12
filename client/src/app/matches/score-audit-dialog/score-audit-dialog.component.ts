import {Component, Inject, OnDestroy, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {AuditService} from '../../audit/service/audit.service';
import {first} from 'rxjs/operators';
import {Audit} from '../../audit/model/audit.model';
import {Observable, Subscription} from 'rxjs';
import {Match} from '../model/match.model';
import {ScoreAuditDialogData} from './score-audit-dialog-data';
import {ProfileService} from '../../profile/profile.service';
import {Profile} from '../../profile/profile';

@Component({
  selector: 'app-score-audit-dialog',
  templateUrl: './score-audit-dialog.component.html',
  styleUrls: ['./score-audit-dialog.component.scss']
})
export class ScoreAuditDialogComponent implements OnInit, OnDestroy {

  public matchId: number;

  private numberOfGames: number;

  private pointsPerGame: number;

  private profileIdToNameMap: any;

  public audits$: Observable<Audit[]>;

  private subscriptions: Subscription = new Subscription();

  constructor(public dialogRef: MatDialogRef<ScoreAuditDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: ScoreAuditDialogData,
              private auditService: AuditService,
              private profileService: ProfileService) {
    this.matchId = data.matchId;
    this.pointsPerGame = data.pointsPerGame;
    this.numberOfGames = data.numberOfGames;
    this.profileIdToNameMap = data.profileIdToNameMap;
  }

  onClose() {
  }

  ngOnInit(): void {
    let query: string = 'eventIdentifier=' + this.matchId;
    query += '&type=MATCH_SCORE'
    this.audits$ = this.auditService.loadWithQuery(query);
    const subscription = this.audits$.pipe(first())
      .subscribe(
        (audits: Audit[]) => {
          // console.log ('audits', audits);
          let scoreEntryProfileIds = [];
          for (const audit of audits) {
            const match: Match = JSON.parse(audit.detailsJSON);
            if (!scoreEntryProfileIds.hasOwnProperty(match.scoreEnteredByProfileId) &&
              !this.profileIdToNameMap.hasOwnProperty(match.scoreEnteredByProfileId)) {
              console.log('Found unknown profile', match.scoreEnteredByProfileId);
              scoreEntryProfileIds.push(match.scoreEnteredByProfileId);
            }
          }
          if (scoreEntryProfileIds.length > 0) {
            this.profileService.getProfile(scoreEntryProfileIds[0]).pipe(first())
              .subscribe((profile: Profile) => {
                console.log('got profile ', profile);
                let copyOfMap = JSON.parse(JSON.stringify(this.profileIdToNameMap));
                copyOfMap[profile.userId] = profile.lastName + ', ' + profile.firstName;
                this.profileIdToNameMap = copyOfMap;
              });
          }
        },
        error => {
          console.error('Error loading audits', error);
        });
    this.subscriptions.add(subscription);
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  formatScore(detailsJSON: string) {
    const match: Match = JSON.parse(detailsJSON);
    let result = '';
    if (match.sideADefaulted || match.sideBDefaulted) {
      const profileOfDefaultedPlayer = match.sideADefaulted ? match.playerAProfileId : match.playerBProfileId;
      let defaultedPlayerName = '';
      if (profileOfDefaultedPlayer.indexOf(';') > 0) {
        // doubles match
        const playerIds = profileOfDefaultedPlayer.split(';');
        defaultedPlayerName += this.profileIdToNameMap[playerIds[0]] + ' / ';
        defaultedPlayerName += this.profileIdToNameMap[playerIds[1]];
      } else {
        // singles match
        defaultedPlayerName = this.profileIdToNameMap[profileOfDefaultedPlayer];
      }
      result = 'Defaulted: ' + defaultedPlayerName;
    } else {
      for (let game: number = 1; game <= this.numberOfGames; game++) {
        switch (game) {
          case 1:
            result += this.formatSingleScore(match.game1ScoreSideA, match.game1ScoreSideB, true);
            break;
          case 2:
            result += this.formatSingleScore(match.game2ScoreSideA, match.game2ScoreSideB);
            break;
          case 3:
            result += this.formatSingleScore(match.game3ScoreSideA, match.game3ScoreSideB);
            break;
          case 4:
            result += this.formatSingleScore(match.game4ScoreSideA, match.game4ScoreSideB);
            break;
          case 5:
            result += this.formatSingleScore(match.game5ScoreSideA, match.game5ScoreSideB);
            break;
          case 6:
            result += this.formatSingleScore(match.game6ScoreSideA, match.game6ScoreSideB);
            break;
          case 7:
            result += this.formatSingleScore(match.game7ScoreSideA, match.game7ScoreSideB);
            break;
        }
      }
    }

    return result;
  }

  formatSingleScore (scoreA: number, scoreB: number, firstGame: boolean = false) {
    if (scoreA === 0 || scoreB === 0) {
      return '';
    } else {
      return ((!firstGame) ? ', ' : '') +`${scoreA} : ${scoreB}`;
    }
  }

  lookupName(profileId: string) {
    let name = 'N/A';
    if (this.profileIdToNameMap.hasOwnProperty(profileId)) {
       name = this.profileIdToNameMap[profileId];
    }
    return name;
  }
}
