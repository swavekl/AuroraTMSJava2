import {ChangeDetectorRef, Component, EventEmitter, Input, OnChanges, Output, SimpleChange, SimpleChanges, ViewChild} from '@angular/core';
import {Tournament} from '../tournament.model';
import {StatesList} from '../../../shared/states/states-list';
import {MatTabGroup} from '@angular/material/tabs';
import {PricingMethod} from '../../model/pricing-method.enum';
import {UserRoles} from '../../../user/user-roles.enum';
import {ProfileFindPopupComponent, ProfileSearchData} from '../../../profile/profile-find-popup/profile-find-popup.component';
import {MatDialog} from '@angular/material/dialog';
import {Subscription} from 'rxjs';
import {Personnel} from '../model/personnel.model';
import {CheckInType} from '../../model/check-in-type.enum';

@Component({
  selector: 'app-tournament-config-edit',
  templateUrl: './tournament-config-edit.component.html',
  styleUrls: ['./tournament-config-edit.component.css']
})
export class TournamentConfigEditComponent implements OnChanges {

  @Input()
  tournament: Tournament;

  // save and cancel
  @Output() saved = new EventEmitter();
  @Output() canceled = new EventEmitter();

  @ViewChild(MatTabGroup)
  tabGroup: MatTabGroup;

  // list of US states
  statesList: any [];
  tournamentTypes: any [];
  pricingMethods: any[] = [];
  checkInMethods: any[] = [];

  // list of personnel is given roles
  refereeList: Personnel[] = [];
  umpireList: Personnel[] = [];
  dataEntryClerksList: Personnel[] = [];

  subscriptions: Subscription = new Subscription();

  constructor(private dialog: MatDialog,
              private changeDetectorRef: ChangeDetectorRef) {
    this.statesList = StatesList.getList();
    this.tournamentTypes = [];
    this.tournamentTypes.push({name: 'Ratings Restricted', value: 'RatingsRestricted'});
    this.tournamentTypes.push({name: 'Round Robin', value: 'RoundRobin'});
    this.tournamentTypes.push({name: 'Teams', value: 'Teams'});
    this.pricingMethods = this.getPricingMethods(this.tournament?.configuration?.tournamentType);
    this.checkInMethods = [];
    this.checkInMethods.push({name: 'Daily', value: 'DAILY'});
    this.checkInMethods.push({name: 'Before Each Event', value: 'PEREVENT'});
  }

  ngOnChanges(changes: SimpleChanges): void {
    const changeTournament: SimpleChange = changes.tournament;
    if (changeTournament) {
      const tournament: Tournament = changeTournament.currentValue;
      if (tournament) {
        const personnelList = tournament?.configuration?.personnelList ?? [];
        this.splitPersonnelList(personnelList);
      }
    }
  }

  private splitPersonnelList(personnelList: Personnel[]) {
    this.refereeList = personnelList.filter((personnel: Personnel) => personnel.role === UserRoles.ROLE_REFEREES );
    this.umpireList = personnelList.filter((personnel: Personnel) => personnel.role === UserRoles.ROLE_UMPIRES );
    this.dataEntryClerksList = personnelList.filter((personnel: Personnel) => personnel.role === UserRoles.ROLE_DATA_ENTRY_CLERKS );
  }

  setActiveTab(tabIndex: number) {
    if (this.tabGroup) {
      this.tabGroup.selectedIndex = tabIndex;
    }
  }

  onSave(formValues: any) {
    // preserve the list which is maintained during add/remove
    const personnelList = this.tournament.configuration.personnelList ?? [];

    const tournament = Tournament.toTournament(formValues);
    // preserve this from previous run
    tournament.numEntries = this.tournament?.numEntries || 0;
    tournament.numEventEntries = this.tournament?.numEventEntries || 0;
    tournament.maxNumEventEntries = this.tournament?.maxNumEventEntries || 0;
    tournament.configuration.personnelList = personnelList;

    this.saved.emit(tournament);
  }

  onCancel() {
    this.canceled.emit('cancelled');
  }

  public get tournamentType(): string {
    return this.tournament?.configuration?.tournamentType;
  }

  public set tournamentType(tournamentType: string) {
    this.tournament.configuration.tournamentType = tournamentType;
  }

  public onChangeTournamentType(tournamentType: string) {
    this.pricingMethods = this.getPricingMethods(tournamentType);
    this.pricingMethod = this.getDefaultPricingMethod(tournamentType);
  }

  public get checkInType(): CheckInType {
    return this.tournament?.configuration?.checkInType;
  }

  public set checkInType(checkInType: string) {
    this.tournament.configuration.checkInType = <CheckInType>checkInType;
  }

  private getDefaultPricingMethod(tournamentType: string): PricingMethod {
    const isTeams: boolean = (tournamentType === 'Teams');
    return (isTeams) ? PricingMethod.GROUP : PricingMethod.STANDARD;
  }

  public get pricingMethod(): string {
    const defaultPricing: PricingMethod = this.getDefaultPricingMethod(this.tournament?.configuration?.tournamentType);
    return this.tournament?.configuration?.pricingMethod ?? defaultPricing;
  }

  public set pricingMethod(pricingMethod: string) {
    this.tournament.configuration.pricingMethod = <PricingMethod>pricingMethod;
  }

  getPricingMethods(tournamentType: string): any [] {
    const pricingMethods = [];
    if (tournamentType === 'Teams') {
      pricingMethods.push({name: 'Team', value: PricingMethod.GROUP.valueOf()});
    } else {
      pricingMethods.push({name: 'Standard', value: PricingMethod.STANDARD.valueOf()});
      pricingMethods.push({name: 'Discounted', value: PricingMethod.DISCOUNTED.valueOf()});
    }
    return pricingMethods;
  }

  onPricingMethodChange(pricingMethod: string) {
    // todo - enable disable discounted pricing options
    console.log('new pricing method is ' + pricingMethod);
  }

  onAddReferee() {
    this.onAddPersonnel(UserRoles.ROLE_REFEREES);
  }

  onAddUmpire() {
    this.onAddPersonnel(UserRoles.ROLE_UMPIRES);
  }

  onAddDataEntryClerk() {
    this.onAddPersonnel(UserRoles.ROLE_DATA_ENTRY_CLERKS);
  }

  onAddMonitor() {
    this.onAddPersonnel(UserRoles.ROLE_MONITORS, 'Find User');
  }

  onAddDigitalScoreBoard() {
    this.onAddPersonnel(UserRoles.ROLE_DIGITAL_SCORE_BOARDS, 'Find User');
  }

  /**
   * Adds a new person into the role
   * @param role
   * @param dialogTitle
   */
  onAddPersonnel(role: UserRoles, dialogTitle?: string) {
    // show Profile selection dialog
    const profileSearchData: ProfileSearchData = {
      firstName: null,
      lastName: null,
      dialogTitle: dialogTitle
    };
    const config = {
      width: '400px', height: '550px', data: profileSearchData
    };

    const personnelList = this.tournament.configuration.personnelList ?? [];
    const dialogRef = this.dialog.open(ProfileFindPopupComponent, config);
    const subscription = dialogRef.afterClosed().subscribe(result => {
      if (result?.action === 'ok') {
        // make sure this person is not already in the list
        const profileId = result.selectedPlayerRecord.id;
        const index = personnelList.findIndex((personnel: Personnel) => personnel.profileId === profileId );
        if (index === -1) {
          const personName = result.selectedPlayerRecord.lastName + ', ' + result.selectedPlayerRecord.firstName;
          const newPersonnel: Personnel = {
            name: personName,
            role: role,
            profileId: profileId
          };
          const updatedPersonnelList = [...personnelList, newPersonnel];
          this.splitPersonnelList(updatedPersonnelList);
          this.tournament.configuration.personnelList = updatedPersonnelList;
          this.changeDetectorRef.markForCheck();
        } else {
          console.log ('Person is already in the role');
        }
      }
    });
    this.subscriptions.add(subscription);
  }

  /**
   *
   * @param profileId
   */
  onRemovePersonnel(profileId: string) {
    const personnelList = [...this.tournament.configuration.personnelList ?? []];
    for (let i = 0; i < personnelList.length; i++) {
      if (personnelList[i].profileId === profileId) {
        personnelList.splice(i, 1);
        this.splitPersonnelList(personnelList);
        this.tournament.configuration.personnelList = personnelList;
        this.changeDetectorRef.markForCheck();
        break;
      }
    }
  }

  onCreateUser() {
    // create user profile for new user e.g. data entry clerk
  }

  getMonitorUser() {
    return this.getLiveScoringUser(UserRoles.ROLE_MONITORS);
  }

  getDigitalScoreBoardUser(): string {
    return this.getLiveScoringUser(UserRoles.ROLE_DIGITAL_SCORE_BOARDS);
  }

  getLiveScoringUser(role: string) {
    let monitorUserName = null;
    const personnelList = this.tournament?.configuration?.personnelList;
    if (personnelList != null) {
      for (let i = 0; i < personnelList.length; i++) {
        const personnel = personnelList[i];
        if (personnel.role === role) {
          monitorUserName = personnel.name;
          break;
        }
      }
    }
    return monitorUserName;
  }

}

