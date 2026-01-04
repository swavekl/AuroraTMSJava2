import {Component, EventEmitter, Input, OnChanges, OnDestroy, Output, SimpleChange, SimpleChanges} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {Team} from '../model/team.model';
import {TeamMember} from '../model/team-member.model';
import {MatDialog} from '@angular/material/dialog';
import {TournamentEvent} from '../../tournament-config/tournament-event.model';
import {ConfirmationPopupComponent} from '../../../shared/confirmation-popup/confirmation-popup.component';
import {TeamRatingCalculator} from '../../teamratingcalculator/team-rating-calculator';
import {TeamEntryStatus} from '../model/team-entry-status.enum';
import {ProfileFindPopupComponent, ProfileSearchData} from '../../../profile/profile-find-popup/profile-find-popup.component';
import {Profile} from '../../../profile/profile';
import {TournamentEntry} from '../model/tournament-entry.model';
import {Subscription} from 'rxjs';
import {ChangeCaptainDialogComponent} from './change-captain-dialog/change-captain-dialog.component';

@Component({
  selector: 'app-team-builder',
  templateUrl: './team-builder.component.html',
  styleUrl: './team-builder.component.scss',
  standalone: false,
  // This allows child ngModels to register with the parent
  viewProviders: [{provide: ControlContainer, useExisting: NgForm}]
})
export class TeamBuilderComponent implements OnChanges, OnDestroy {

  @Input() team: Team;

  @Input() playerProfile: Profile;

  @Input() entry: TournamentEntry;

  @Input() teamEvent: TournamentEvent;

  @Output() teamChanged = new EventEmitter<Team>();

  private subscriptions: Subscription = new Subscription();

  // true if current player is a captain
  protected hasCaptain: boolean = false;

  // true if current member is a team captain
  protected amICaptain: boolean = false;

  // can add members
  protected canAddMembers: boolean = false;

  // can join team
  protected canJoinTeam: boolean = false;

  constructor(private dialog: MatDialog) {
  }

  ngOnChanges(changes: SimpleChanges): void {
    const teamChange: SimpleChange = changes.team;
    if (teamChange != null) {
      const updateTeam: Team = teamChange.currentValue;
      if (updateTeam != null) {
        const teamMembers: TeamMember[] = updateTeam?.teamMembers || [];
        this.hasCaptain = teamMembers.some(m => m.captain);
        // console.log('this.hasCaptain', this.hasCaptain);
      }
    }

    if (this.team?.teamMembers != null) {
      this.amICaptain = (this.playerProfile != null)
        ? this.team.teamMembers.some(m => (m.profileId === this.playerProfile.userId) ? m.captain : false)
      : false;
      // console.log('amICaptain', this.amICaptain);
    }

    if (this.teamEvent != null) {
      const maxPlayers = this.teamEvent?.maxTeamPlayers || 0;
      const currentPlayers = this.team?.teamMembers?.length || 0;
      this.canAddMembers = (currentPlayers < maxPlayers) && this.amICaptain;
      // console.log('canAddMembers', this.canAddMembers);

      this.canJoinTeam = this.team.teamMembers.some(m => m.profileId === this.playerProfile.userId)
        || this.team?.teamMembers.length === 0;
      // console.log('canJoinTeam', this.canJoinTeam);
    }
  }

  ngOnDestroy() {
    this.subscriptions.unsubscribe();
  }

  /**
   *
   * @param team
   * @param playerIndex
   * @protected
   */
  protected canRemovePlayer(playerIndex: number): boolean {
    // only non-captain can be removed and only be a
    const member: TeamMember = this.team?.teamMembers[playerIndex];
    return (this.amICaptain && !member.captain) || (!this.amICaptain && member.profileId == this.playerProfile?.userId);
  }

  /**
   *
   * @param team
   * @param memberIndex
   * @protected
   */
  protected onRemoveMember(memberIndex: number) {
    const memberFullName = this.team?.teamMembers[memberIndex].playerName;
    const dialogRef = this.dialog.open(ConfirmationPopupComponent, {
      width: '300px', height: '200px',
      data: {
        message: `Are you sure you want to remove team member ${memberFullName} ?`,
        contentAreaHeight: '150px', title: 'Confirm Deletion', okText: 'Delete', showCancel: true
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === 'ok') {
        const membersClone: TeamMember [] = [...this.team.teamMembers];
        membersClone.splice(memberIndex, 1);
        let updatedTeam: Team = {...this.team, teamMembers: membersClone};
        const teamRating = this.getTeamRating(updatedTeam);
        updatedTeam = {...updatedTeam, teamRating: teamRating};
        this.emitTeamUpdate(updatedTeam);
      }
    });
  }

  /**
   *
   */
  public onAddMember() {
    const profileSearchData: ProfileSearchData = {
      firstName: null,
      lastName: null
    };
    const config = {
      width: '400px', height: '550px', data: profileSearchData
    };

    const dialogRef = this.dialog.open(ProfileFindPopupComponent, config);
    const subscription = dialogRef.afterClosed().subscribe(result => {
      if (result?.action === 'ok') {
        // first player to sign up will be a team captain
        const isCaptain = this.team.teamMembers.length === 0;
        const fullPlayerName = result.selectedPlayerRecord.lastName + ', ' + result.selectedPlayerRecord.firstName;
        const memberProfileId = result.selectedPlayerRecord.id;
        // todo: get this player's eligibility rating- not current rating
        const rating = result.selectedPlayerRecord.rating || 0;
        const entryFk = (this.playerProfile.userId === memberProfileId) ? this.entry.id : null;
        const status = (isCaptain) ? TeamEntryStatus.CONFIRMED : TeamEntryStatus.INVITED;
        const newMember: TeamMember = {
          id: null,
          teamFk: this.team.id,
          profileId: memberProfileId,
          tournamentEntryFk: entryFk,
          tournamentEventFk: this.team.tournamentEventFk,
          playerName: fullPlayerName,
          captain: isCaptain,
          playerRating: rating,
          status: status,
          cartSessionId: null
        };
        this.addMemberInternal(newMember);
      }
    });
    this.subscriptions.add(subscription);
  }

  /**
   *
   * @protected
   */
  protected onJoinTeam() {
    // Logic to add current user as a member (status CONFIRMED)
    const fullPlayerName = `${this.playerProfile.lastName}, ${this.playerProfile.firstName}`;
    const isCaptain = !this.hasCaptain;
    const newMember: TeamMember = {
      id: null,
      teamFk: this.team.id,
      profileId: this.playerProfile.userId,
      status: TeamEntryStatus.CONFIRMED,
      captain: isCaptain,
      tournamentEventFk: this.team.tournamentEventFk,
      playerName: fullPlayerName,
      playerRating: this.entry.eligibilityRating,
      tournamentEntryFk: this.entry.id,
      cartSessionId: null
    };
    this.addMemberInternal(newMember);
  }

  /**
   *
   */
  private addMemberInternal(newMember: TeamMember) {
    const membersClone: TeamMember [] = [...this.team.teamMembers, newMember];
    let updatedTeam: Team = {...this.team, teamMembers: membersClone};
    const teamRating = this.getTeamRating(updatedTeam);
    updatedTeam = {...updatedTeam, teamRating: teamRating};
    this.emitTeamUpdate(updatedTeam);
  }

  /**
   *
   * @param updatedTeam
   * @private
   */
  private emitTeamUpdate(updatedTeam: Team) {
    this.teamChanged.emit(updatedTeam);
  }

  protected onChangeCaptain() {
    const dialogRef = this.dialog.open(ChangeCaptainDialogComponent, {
      width: '350px',
      data: {
        teamName: this.team.name,
        members: this.team.teamMembers
      }
    });

    dialogRef.afterClosed().subscribe(newCaptainId => {
      if (newCaptainId) {
        // 1. Update the transient flags in the UI
        this.team.teamMembers.forEach(m => {
          m.captain = (m.profileId === newCaptainId);
        });
        this.emitTeamUpdate(this.team);
      }
    });
  }

  /**
   *
   * @protected
   */
  protected getTeamRating(team: Team): number {
    const event: TournamentEvent = this.teamEvent;
    const memberRatings: number [] = team.teamMembers.map(member => member.playerRating);
    const teamRatingCalculationMethod = event.teamRatingCalculationMethod;
    const teamRatingCalculator = new TeamRatingCalculator(teamRatingCalculationMethod);
    return teamRatingCalculator.calculateRating(memberRatings);
  }

  onConfirmInvitation() {
    // if (this.myMembership) {
    //   this.myMembership.status = TeamEntryStatus.CONFIRMED;
    //   this.rosterChanged.emit(this.team);
    // }
  }

}
