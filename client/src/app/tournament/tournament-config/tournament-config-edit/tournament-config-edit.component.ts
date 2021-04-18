import {Component, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {Tournament} from '../tournament.model';
import {StatesList} from '../../../shared/states/states-list';
import {MatTabGroup} from '@angular/material/tabs';
import {PricingMethod} from '../../model/pricing-method.enum';

@Component({
  selector: 'app-tournament-config-edit',
  templateUrl: './tournament-config-edit.component.html',
  styleUrls: ['./tournament-config-edit.component.css']
})
export class TournamentConfigEditComponent {

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

  constructor() {
    this.statesList = StatesList.getList();
    this.tournamentTypes = [];
    this.tournamentTypes.push({name: 'Ratings Restricted', value: 'RatingsRestricted'});
    this.tournamentTypes.push({name: 'Round Robin', value: 'RoundRobin'});
    this.tournamentTypes.push({name: 'Teams', value: 'Teams'});
    this.pricingMethods = this.getPricingMethods(this.tournament?.configuration?.tournamentType);
  }

  setActiveTab(tabIndex: number) {
    if (this.tabGroup) {
      this.tabGroup.selectedIndex = tabIndex;
    }
  }

  onEnter() {
    console.log('entering the tournament');
  }

  onSave(formValues: any) {
    const tournament = Tournament.toTournament(formValues);
    // preserve this from previous run
    tournament.numEntries = this.tournament?.numEntries || 0;
    tournament.numEventEntries = this.tournament?.numEventEntries || 0;
    tournament.maxNumEventEntries = this.tournament?.maxNumEventEntries || 0;
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
}
