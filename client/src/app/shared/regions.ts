export class Regions {

  constructor() {
  }

  theList: any [] =
    [
      {name: 'East', shortName: 'EA', states: ['CT', 'DE', 'DC', 'ME', 'MD', 'MA', 'NH', 'NJ', 'NY', 'PA', 'RI', 'VT', 'VA', 'WV']},
      {name: 'Midwest', shortName: 'MW', states: ['IL', 'IN', 'KY', 'MI', 'OH']},
      {name: 'Mountain', shortName: 'MO', states: ['CO', 'NE', 'NM', 'UT', 'WY']},
      {name: 'North', shortName: 'NO', states: ['IA', 'MN', 'ND', 'SD', 'WI']},
      {name: 'Northwest', shortName: 'NW', states: ['AK', 'ID', 'MT', 'OR', 'WA']},
      {name: 'Pacific', shortName: 'PA', states: ['AZ', 'CA', 'HI', 'NV']},
      {name: 'South Central', shortName: 'SC', states: ['AR', 'KS', 'LA', 'MO', 'OK', 'TX']},
      {name: 'Southeast', shortName: 'SE', states: ['AL', 'FL', 'GA', 'MS', 'NC', 'SC', 'TN']},
      {name: 'International', shortName: 'IT', states: []}
    ];

  getList() {
    return this.theList;
  }

  lookupRegion(state: String): string {
    let regionName = null;
    for (const regionDefinition of this.theList) {
      if (regionDefinition.states.indexOf(state) !== -1) {
        regionName = regionDefinition.name;
        break;
      }
    }
    return regionName;
  }
}
