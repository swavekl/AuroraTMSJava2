/**
 *  Utility for creating events
 */
import {GenderRestriction} from './gender-restriction.enum';

export class EventDefaults {
  public eventDefaults: any [] = [
    {name: 'Open Singles', doubles: false, minPlayerAge: 0, maxPlayerAge: 0, minPlayerRating: 0, maxPlayerRating: 0, genderRestriction: GenderRestriction.NONE },
    {name: 'Open Doubles', doubles: true, minPlayerAge: 0, maxPlayerAge: 0, minPlayerRating: 0, maxPlayerRating: 0, genderRestriction: GenderRestriction.NONE },
    {name: 'U4800 Doubles', doubles: true, minPlayerAge: 0, maxPlayerAge: 0, minPlayerRating: 0, maxPlayerRating: 4799, genderRestriction: GenderRestriction.NONE },
    {name: 'U3800 Doubles', doubles: true, minPlayerAge: 0, maxPlayerAge: 0, minPlayerRating: 0, maxPlayerRating: 3799, genderRestriction: GenderRestriction.NONE },
    {name: 'U2500', doubles: false, minPlayerAge: 0, maxPlayerAge: 0, minPlayerRating: 0, maxPlayerRating: 2499, genderRestriction: GenderRestriction.NONE },
    {name: 'U2400', doubles: false, minPlayerAge: 0, maxPlayerAge: 0, minPlayerRating: 0, maxPlayerRating: 2399, genderRestriction: GenderRestriction.NONE },
    {name: 'U2300', doubles: false, minPlayerAge: 0, maxPlayerAge: 0, minPlayerRating: 0, maxPlayerRating: 2299, genderRestriction: GenderRestriction.NONE },
    {name: 'U2200', doubles: false, minPlayerAge: 0, maxPlayerAge: 0, minPlayerRating: 0, maxPlayerRating: 2199, genderRestriction: GenderRestriction.NONE },
    {name: 'U2100', doubles: false, minPlayerAge: 0, maxPlayerAge: 0, minPlayerRating: 0, maxPlayerRating: 2099, genderRestriction: GenderRestriction.NONE },
    {name: 'U2000', doubles: false, minPlayerAge: 0, maxPlayerAge: 0, minPlayerRating: 0, maxPlayerRating: 1999, genderRestriction: GenderRestriction.NONE },
    {name: 'U1900', doubles: false, minPlayerAge: 0, maxPlayerAge: 0, minPlayerRating: 0, maxPlayerRating: 1899, genderRestriction: GenderRestriction.NONE },
    {name: 'U1800', doubles: false, minPlayerAge: 0, maxPlayerAge: 0, minPlayerRating: 0, maxPlayerRating: 1799, genderRestriction: GenderRestriction.NONE },
    {name: 'U1700', doubles: false, minPlayerAge: 0, maxPlayerAge: 0, minPlayerRating: 0, maxPlayerRating: 1699, genderRestriction: GenderRestriction.NONE },
    {name: 'U1600', doubles: false, minPlayerAge: 0, maxPlayerAge: 0, minPlayerRating: 0, maxPlayerRating: 1599, genderRestriction: GenderRestriction.NONE },
    {name: 'U1500', doubles: false, minPlayerAge: 0, maxPlayerAge: 0, minPlayerRating: 0, maxPlayerRating: 1499, genderRestriction: GenderRestriction.NONE },
    {name: 'U1400', doubles: false, minPlayerAge: 0, maxPlayerAge: 0, minPlayerRating: 0, maxPlayerRating: 1399, genderRestriction: GenderRestriction.NONE },
    {name: 'U1300', doubles: false, minPlayerAge: 0, maxPlayerAge: 0, minPlayerRating: 0, maxPlayerRating: 1299, genderRestriction: GenderRestriction.NONE },
    {name: 'U1200', doubles: false, minPlayerAge: 0, maxPlayerAge: 0, minPlayerRating: 0, maxPlayerRating: 1199, genderRestriction: GenderRestriction.NONE },
    {name: 'U1100', doubles: false, minPlayerAge: 0, maxPlayerAge: 0, minPlayerRating: 0, maxPlayerRating: 1099, genderRestriction: GenderRestriction.NONE },
    {name: 'U1000', doubles: false, minPlayerAge: 0, maxPlayerAge: 0, minPlayerRating: 0, maxPlayerRating: 999, genderRestriction: GenderRestriction.NONE },
    {name: 'U900', doubles: false, minPlayerAge: 0, maxPlayerAge: 0, minPlayerRating: 0, maxPlayerRating: 899, genderRestriction: GenderRestriction.NONE },
    {name: 'U800', doubles: false, minPlayerAge: 0, maxPlayerAge: 0, minPlayerRating: 0, maxPlayerRating: 799, genderRestriction: GenderRestriction.NONE },
    {name: 'U700', doubles: false, minPlayerAge: 0, maxPlayerAge: 0, minPlayerRating: 0, maxPlayerRating: 699, genderRestriction: GenderRestriction.NONE },
    {name: 'U600', doubles: false, minPlayerAge: 0, maxPlayerAge: 0, minPlayerRating: 0, maxPlayerRating: 599, genderRestriction: GenderRestriction.NONE },
    {name: 'U500', doubles: false, minPlayerAge: 0, maxPlayerAge: 0, minPlayerRating: 0, maxPlayerRating: 499, genderRestriction: GenderRestriction.NONE },
    {name: 'U400', doubles: false, minPlayerAge: 0, maxPlayerAge: 0, minPlayerRating: 0, maxPlayerRating: 399, genderRestriction: GenderRestriction.NONE },
    {name: 'U300', doubles: false, minPlayerAge: 0, maxPlayerAge: 0, minPlayerRating: 0, maxPlayerRating: 299, genderRestriction: GenderRestriction.NONE },
    {name: 'U200', doubles: false, minPlayerAge: 0, maxPlayerAge: 0, minPlayerRating: 0, maxPlayerRating: 199, genderRestriction: GenderRestriction.NONE },
    {name: 'U100', doubles: false, minPlayerAge: 0, maxPlayerAge: 0, minPlayerRating: 0, maxPlayerRating: 99, genderRestriction: GenderRestriction.NONE },
    {name: 'Youth Under 14', doubles: false, minPlayerAge: 0, maxPlayerAge: 14, minPlayerRating: 0, maxPlayerRating: 0, genderRestriction: GenderRestriction.NONE },
    {name: 'Youth Under 18', doubles: false, minPlayerAge: 0, maxPlayerAge: 18, minPlayerRating: 0, maxPlayerRating: 0, genderRestriction: GenderRestriction.NONE },
    {name: 'Over 40', doubles: false, minPlayerAge: 40, maxPlayerAge: 0, minPlayerRating: 0, maxPlayerRating: 0, genderRestriction: GenderRestriction.NONE },
    {name: 'Over 50', doubles: false, minPlayerAge: 50, maxPlayerAge: 0, minPlayerRating: 0, maxPlayerRating: 0, genderRestriction: GenderRestriction.NONE },
    {name: 'Over 60', doubles: false, minPlayerAge: 60, maxPlayerAge: 0, minPlayerRating: 0, maxPlayerRating: 0, genderRestriction: GenderRestriction.NONE },
    {name: 'Over 70', doubles: false, minPlayerAge: 70, maxPlayerAge: 0, minPlayerRating: 0, maxPlayerRating: 0, genderRestriction: GenderRestriction.NONE },
    {name: 'Over 80', doubles: false, minPlayerAge: 80, maxPlayerAge: 0, minPlayerRating: 0, maxPlayerRating: 0, genderRestriction: GenderRestriction.NONE },
    {name: 'Hard bat', doubles: false, minPlayerAge: 0, maxPlayerAge: 0, minPlayerRating: 0, maxPlayerRating: 0, genderRestriction: GenderRestriction.NONE },
    {name: 'Womens', doubles: false, minPlayerAge: 0, maxPlayerAge: 0, minPlayerRating: 0, maxPlayerRating: 0, genderRestriction: GenderRestriction.FEMALE},
    {name: 'Boys Under 10', doubles: false, minPlayerAge: 0, maxPlayerAge: 10, minPlayerRating: 0, maxPlayerRating: 0, genderRestriction: GenderRestriction.MALE},
    {name: 'Boys Under 14', doubles: false, minPlayerAge: 0, maxPlayerAge: 14, minPlayerRating: 0, maxPlayerRating: 0, genderRestriction: GenderRestriction.MALE},
    {name: 'Girls Under 10', doubles: false, minPlayerAge: 0, maxPlayerAge: 10, minPlayerRating: 0, maxPlayerRating: 0, genderRestriction: GenderRestriction.FEMALE},
    {name: 'Girls Under 14', doubles: false, minPlayerAge: 0, maxPlayerAge: 14, minPlayerRating: 0, maxPlayerRating: 0, genderRestriction: GenderRestriction.FEMALE},
    {name: 'Other', doubles: false, minPlayerAge: 0, maxPlayerAge: 0, minPlayerRating: 0, maxPlayerRating: 0, genderRestriction: GenderRestriction.NONE}
  ];
}

