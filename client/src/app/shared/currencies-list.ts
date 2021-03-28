// individual currency
export interface Currency {
  aplha3: string;  // country code
  currencyId: string;
  currencyName: string;
  currencySymbol: string;
  id: string;
  countryName: string;  // country official name
}

export class CurrenciesList {

  static theList: Currency [] = [
    {
      aplha3: 'AFG',
      currencyId: 'AFN',
      currencyName: 'Afghan afghani',
      currencySymbol: '؋',
      id: 'AF',
      countryName: 'Afghanistan'
    },
    {
      aplha3: 'AIA',
      currencyId: 'XCD',
      currencyName: 'East Caribbean dollar',
      currencySymbol: '$',
      id: 'AI',
      countryName: 'Anguilla'
    },
    {
      aplha3: 'AUS',
      currencyId: 'AUD',
      currencyName: 'Australian dollar',
      currencySymbol: '$',
      id: 'AU',
      countryName: 'Australia'
    },
    {
      currencyId: 'BDT',
      currencyName: 'Bangladeshi taka',
      countryName: 'Bangladesh',
      aplha3: 'BGD',
      id: 'BD',
      currencySymbol: '৳'
    },
    {
      currencyId: 'XOF',
      currencyName: 'West African CFA franc',
      countryName: 'Benin',
      aplha3: 'BEN',
      id: 'BJ',
      currencySymbol: 'Fr'
    },
    {
      aplha3: 'BRA',
      currencyId: 'BRL',
      currencyName: 'Brazilian real',
      currencySymbol: 'R$',
      id: 'BR',
      countryName: 'Brazil'
    },
    {
      aplha3: 'KHM',
      currencyId: 'KHR',
      currencyName: 'Cambodian riel',
      currencySymbol: '៛',
      id: 'KH',
      countryName: 'Cambodia'
    },
    {
      currencyId: 'XAF',
      currencyName: 'Central African CFA franc',
      countryName: 'Chad',
      aplha3: 'TCD',
      id: 'TD',
      currencySymbol: 'Fr'
    },
    {
      currencyId: 'XAF',
      currencyName: 'Central African CFA franc',
      countryName: 'Congo',
      aplha3: 'COG',
      id: 'CG',
      currencySymbol: 'Fr'
    },
    {
      currencyId: 'CUP',
      currencyName:
        'Cuban peso',
      currencySymbol: '$',
      countryName: 'Cuba',
      aplha3: 'CUB',
      id: 'CU'
    },
    {
      aplha3: 'DMA',
      currencyId: 'XCD',
      currencyName: 'East Caribbean dollar',
      currencySymbol: '$',
      id: 'DM',
      countryName: 'Dominica'
    },
    {aplha3: 'FIN', currencyId: 'EUR', currencyName: 'European euro', currencySymbol: '€', id: 'FI', countryName: 'Finland'},
    {currencyId: 'GEL', currencyName: 'Georgian lari', countryName: 'Georgia', aplha3: 'GEO', id: 'GE', currencySymbol: '₾'},
    {
      aplha3: 'GRD',
      currencyId: 'XCD',
      currencyName: 'East Caribbean dollar',
      currencySymbol: '$',
      id: 'GD',
      countryName: 'Grenada'
    },
    {currencyId: 'HTG', currencyName: 'Haitian gourde', countryName: 'Haiti', aplha3: 'HTI', id: 'HT', currencySymbol: 'G'},
    {aplha3: 'IND', currencyId: 'INR', currencyName: 'Indian rupee', currencySymbol: '₹', id: 'IN', countryName: 'India'},
    {
      aplha3: 'ISR',
      currencyId: 'ILS',
      currencyName: 'Israeli new sheqel',
      currencySymbol: '₪',
      id: 'IL',
      countryName: 'Israel'
    },
    {
      aplha3: 'KAZ',
      currencyId: 'KZT',
      currencyName: 'Kazakhstani tenge',
      currencySymbol: 'лв',
      id: 'KZ',
      countryName: 'Kazakhstan'
    },
    {
      currencyId: 'KWD',
      currencyName: 'Kuwaiti dinar',
      countryName: 'Kuwait',
      aplha3: 'KWT',
      id: 'KW',
      currencySymbol: 'د.ك'
    },
    {currencyId: 'LSL', currencyName: 'Lesotho loti', countryName: 'Lesotho', aplha3: 'LSO', id: 'LS', currencySymbol: 'L'},
    {
      aplha3: 'LUX',
      currencyId: 'EUR',
      currencyName: 'European euro',
      currencySymbol: '€',
      id: 'LU',
      countryName: 'Luxembourg'
    },
    {
      aplha3: 'MYS',
      currencyId: 'MYR',
      currencyName: 'Malaysian ringgit',
      currencySymbol: 'RM',
      id: 'MY',
      countryName: 'Malaysia'
    },
    {
      aplha3: 'MUS',
      currencyId: 'MUR',
      currencyName: 'Mauritian rupee',
      currencySymbol: '₨',
      id: 'MU',
      countryName: 'Mauritius'
    },
    {
      aplha3: 'MNG',
      currencyId: 'MNT',
      currencyName: 'Mongolian tugrik',
      currencySymbol: '₮',
      id: 'MN',
      countryName: 'Mongolia'
    },
    {currencyId: 'MMK', currencyName: 'Myanma kyat', countryName: 'Myanmar', aplha3: 'MMR', id: 'MM', currencySymbol: 'Ks'},
    {
      currencyId: 'XPF',
      currencyName: 'CFP franc',
      countryName: 'New Caledonia',
      aplha3: 'NCL',
      id: 'NC',
      currencySymbol: 'Fr'
    },
    {
      aplha3: 'NOR',
      currencyId: 'NOK',
      currencyName: 'Norwegian krone',
      currencySymbol: 'kr',
      id: 'NO',
      countryName: 'Norway'
    },
    {
      currencyId: 'PGK',
      currencyName: 'Papua New Guinean kina',
      countryName: 'Papua New Guinea',
      aplha3: 'PNG',
      id: 'PG',
      currencySymbol: 'K'
    },
    {
      aplha3: 'PRT',
      currencyId: 'EUR',
      currencyName: 'European euro',
      currencySymbol: '€',
      id: 'PT',
      countryName: 'Portugal'
    },
    {currencyId: 'RWF', currencyName: 'Rwandan franc', countryName: 'Rwanda', aplha3: 'RWA', id: 'RW', currencySymbol: 'Fr'},
    {
      currencyId: 'WST',
      currencyName: 'Samoan tala',
      countryName: 'Samoa (Western)',
      aplha3: 'WSM',
      id: 'WS',
      currencySymbol: 'T'
    },
    {
      aplha3: 'SRB',
      currencyId: 'RSD',
      currencyName: 'Serbian dinar',
      currencySymbol: 'Дин.',
      id: 'RS',
      countryName: 'Serbia'
    },
    {
      aplha3: 'SVN',
      currencyId: 'EUR',
      currencyName: 'European euro',
      currencySymbol: '€',
      id: 'SI',
      countryName: 'Slovenia'
    },
    {aplha3: 'ESP', currencyId: 'EUR', currencyName: 'European euro', currencySymbol: '€', id: 'ES', countryName: 'Spain'},
    {aplha3: 'SWE', currencyId: 'SEK', currencyName: 'Swedish krona', currencySymbol: 'kr', id: 'SE', countryName: 'Sweden'},
    {
      aplha3: 'TZA',
      currencyId: 'TZS',
      currencyName: 'Tanzanian shilling',
      currencySymbol: 'TSh',
      id: 'TZ',
      countryName: 'Tanzania'
    },
    {
      currencyId: 'TND',
      currencyName: 'Tunisian dinar',
      countryName: 'Tunisia',
      aplha3: 'TUN',
      id: 'TN',
      currencySymbol: 'ملّيم'
    },
    {
      aplha3: 'UKR',
      currencyId: 'UAH',
      currencyName: 'Ukrainian hryvnia',
      currencySymbol: '₴',
      id: 'UA',
      countryName: 'Ukraine'
    },
    {
      aplha3: 'UZB',
      currencyId: 'UZS',
      currencyName: 'Uzbekistani som',
      currencySymbol: 'лв',
      id: 'UZ',
      countryName: 'Uzbekistan'
    },
    {aplha3: 'YEM', currencyId: 'YER', currencyName: 'Yemeni rial', currencySymbol: '﷼', id: 'YE', countryName: 'Yemen'},
    {
      currencyId: 'DZD',
      currencyName: 'Algerian dinar',
      countryName: 'Algeria',
      aplha3: 'DZA',
      id: 'DZ',
      currencySymbol: 'د.ج'
    },
    {
      aplha3: 'ARG',
      currencyId: 'ARS',
      currencyName: 'Argentine peso',
      currencySymbol: '$',
      id: 'AR',
      countryName: 'Argentina'
    },
    {
      aplha3: 'AZE',
      currencyId: 'AZN',
      currencyName: 'Azerbaijani manat',
      currencySymbol: 'ман',
      id: 'AZ',
      countryName: 'Azerbaijan'
    },
    {
      aplha3: 'BLR',
      currencyId: 'BYN',
      currencyName: 'New Belarusian ruble',
      currencySymbol: 'p.',
      id: 'BY',
      countryName: 'Belarus'
    },
    {
      aplha3: 'BOL',
      currencyId: 'BOB',
      currencyName: 'Bolivian boliviano',
      currencySymbol: '$b',
      id: 'BO',
      countryName: 'Bolivia'
    },
    {
      aplha3: 'BGR',
      currencyId: 'BGN',
      currencyName: 'Bulgarian lev',
      currencySymbol: 'лв',
      id: 'BG',
      countryName: 'Bulgaria'
    },
    {
      aplha3: 'CAN',
      currencyId: 'CAD',
      currencyName: 'Canadian dollar',
      currencySymbol: '$',
      id: 'CA',
      countryName: 'Canada'
    },
    {
      aplha3: 'CHN',
      currencyId: 'CNY',
      currencyName: 'Chinese renminbi',
      currencySymbol: '¥',
      id: 'CN',
      countryName: 'China'
    },
    {
      aplha3: 'CRI',
      currencyId: 'CRC',
      currencyName: 'Costa Rican colon',
      currencySymbol: '₡',
      id: 'CR',
      countryName: 'Costa Rica'
    },
    {
      aplha3: 'CZE',
      currencyId: 'CZK',
      currencyName: 'Czech koruna',
      currencySymbol: 'Kč',
      id: 'CZ',
      countryName: 'Czech Republic'
    },
    {aplha3: 'ECU', currencyId: 'USD', currencyName: 'U.S. Dollar', currencySymbol: '$', id: 'EC', countryName: 'Ecuador'},
    {aplha3: 'EST', currencyId: 'EUR', currencyName: 'European euro', currencySymbol: '€', id: 'EE', countryName: 'Estonia'},
    {
      currencyId: 'XPF',
      currencyName: 'CFP franc',
      countryName: 'French Polynesia',
      aplha3: 'PYF',
      id: 'PF',
      currencySymbol: 'Fr'
    },
    {currencyId: 'GHS', currencyName: 'Ghanaian cedi', countryName: 'Ghana', aplha3: 'GHA', id: 'GH', currencySymbol: '₵'},
    {currencyId: 'GNF', currencyName: 'Guinean franc', countryName: 'Guinea', aplha3: 'GIN', id: 'GN', currencySymbol: 'Fr'},
    {
      aplha3: 'HKG',
      currencyId: 'HKD',
      currencyName: 'Hong Kong dollar',
      currencySymbol: '$',
      id: 'HK',
      countryName: 'Hong Kong'
    },
    {
      aplha3: 'IRN',
      currencyId: 'IRR',
      currencyName: 'Iranian rial',
      currencySymbol: '﷼',
      id: 'IR',
      countryName: 'Iran, Islamic Republic of'
    },
    {
      aplha3: 'JAM',
      currencyId: 'JMD',
      currencyName: 'Jamaican dollar',
      currencySymbol: 'J$',
      id: 'JM',
      countryName: 'Jamaica'
    },
    {
      aplha3: 'KIR',
      currencyId: 'AUD',
      currencyName: 'Australian dollar',
      currencySymbol: '$',
      id: 'KI',
      countryName: 'Kiribati'
    },
    {aplha3: 'LAO', currencyId: 'LAK', currencyName: 'Lao kip', currencySymbol: '₭', id: 'LA', countryName: 'Laos'},
    {currencyId: 'LYD', currencyName: 'Libyan dinar', countryName: 'Libya', aplha3: 'LBY', id: 'LY', currencySymbol: 'ل.د'},
    {
      aplha3: 'MKD',
      currencyId: 'MKD',
      currencyName: 'Macedonian denar',
      currencySymbol: 'ден',
      id: 'MK',
      countryName: 'Macedonia (Former Yug. Rep.)'
    },
    {
      currencyId: 'XOF',
      currencyName: 'West African CFA franc',
      countryName: 'Mali',
      aplha3: 'MLI',
      id: 'ML',
      currencySymbol: 'Fr'
    },
    {
      aplha3: 'FSM',
      currencyId: 'USD',
      currencyName: 'U.S. Dollar',
      currencySymbol: '$',
      id: 'FM',
      countryName: 'Micronesia'
    },
    {
      aplha3: 'MSR',
      currencyId: 'XCD',
      currencyName: 'East Caribbean dollar',
      currencySymbol: '$',
      id: 'MS',
      countryName: 'Montserrat'
    },
    {
      aplha3: 'NRU',
      currencyId: 'AUD',
      currencyName: 'Australian dollar',
      currencySymbol: '$',
      id: 'NR',
      countryName: 'Nauru'
    },
    {
      aplha3: 'NIC',
      currencyId: 'NIO',
      currencyName: 'Nicaraguan cordoba',
      currencySymbol: 'C$',
      id: 'NI',
      countryName: 'Nicaragua'
    },
    {
      aplha3: 'PAK',
      currencyId: 'PKR',
      currencyName: 'Pakistani rupee',
      currencySymbol: '₨',
      id: 'PK',
      countryName: 'Pakistan'
    },
    {
      aplha3: 'PER',
      currencyId: 'PEN',
      currencyName: 'Peruvian nuevo sol',
      currencySymbol: 'S/.',
      id: 'PE',
      countryName: 'Peru'
    },
    {aplha3: 'QAT', currencyId: 'QAR', currencyName: 'Qatari riyal', currencySymbol: '﷼', id: 'QA', countryName: 'Qatar'},
    {
      aplha3: 'KNA',
      currencyId: 'XCD',
      currencyName: 'East Caribbean dollar',
      currencySymbol: '$',
      id: 'KN',
      countryName: 'Saint Kitts and Nevis'
    },
    {
      currencyId: 'STD',
      currencyName: 'Sao Tome and Principe dobra',
      countryName: 'Sao Tome and Principe',
      aplha3: 'STP',
      id: 'ST',
      currencySymbol: 'Db'
    },
    {
      currencyId: 'SLL',
      currencyName: 'Sierra Leonean leone',
      countryName: 'Sierra Leone',
      aplha3: 'SLE',
      id: 'SL',
      currencySymbol: 'Le'
    },
    {
      aplha3: 'SOM',
      currencyId: 'SOS',
      currencyName: 'Somali shilling',
      currencySymbol: 'S',
      id: 'SO',
      countryName: 'Somalia'
    },
    {
      currencyId: 'SDG',
      currencyName: 'Sudanese pound',
      countryName: 'Sudan',
      aplha3: 'SDN',
      id: 'SD',
      currencySymbol: 'ج.س.'
    },
    {aplha3: 'SYR', currencyId: 'SYP', currencyName: 'Syrian pound', currencySymbol: '£', id: 'SY', countryName: 'Syria'},
    {
      currencyId: 'XOF',
      currencyName: 'West African CFA franc',
      countryName: 'Togo',
      aplha3: 'TGO',
      id: 'TG',
      currencySymbol: 'Fr'
    },
    {
      currencyId: 'TMT',
      currencyName: 'Turkmenistan manat',
      countryName: 'Turkmenistan',
      aplha3: 'TKM',
      id: 'TM',
      currencySymbol: 'm'
    },
    {
      aplha3: 'GBR',
      currencyId: 'GBP',
      currencyName: 'British pound',
      currencySymbol: '£',
      id: 'GB',
      countryName: 'United Kingdom'
    },
    {
      currencyId: 'VEF',
      currencyName: 'Venezuelan bolivar',
      countryName: 'Venezuela',
      aplha3: 'VEN',
      id: 'VE',
      currencySymbol: 'Bs'
    },
    {aplha3: 'AND', currencyId: 'EUR', currencyName: 'European euro', currencySymbol: '€', id: 'AD', countryName: 'Andorra'},
    {currencyId: 'AMD', currencyName: 'Armenian dram', countryName: 'Armenia', aplha3: 'ARM', id: 'AM', currencySymbol: '֏'},
    {
      aplha3: 'BHS',
      currencyId: 'BSD',
      currencyName: 'Bahamian dollar',
      currencySymbol: '$',
      id: 'BS',
      countryName: 'Bahamas'
    },
    {aplha3: 'BEL', currencyId: 'EUR', currencyName: 'European euro', currencySymbol: '€', id: 'BE', countryName: 'Belgium'},
    {
      aplha3: 'BIH',
      currencyId: 'BAM',
      currencyName: 'Bosnia and Herzegovina konvertibilna marka',
      currencySymbol: 'KM',
      id: 'BA',
      countryName: 'Bosnia-Herzegovina'
    },
    {
      currencyId: 'XOF',
      currencyName: 'West African CFA franc',
      countryName: 'Burkina Faso',
      aplha3: 'BFA',
      id: 'BF',
      currencySymbol: 'Fr'
    },
    {
      aplha3: 'CYM',
      currencyId: 'KYD',
      currencyName: 'Cayman Islands dollar',
      currencySymbol: '$',
      id: 'KY',
      countryName: 'Cayman Islands'
    },
    {
      aplha3: 'COL',
      currencyId: 'COP',
      currencyName: 'Colombian peso',
      currencySymbol: '$',
      id: 'CO',
      countryName: 'Colombia'
    },
    {
      currencyId: 'XOF',
      currencyName: 'West African CFA franc',
      countryName: 'Côte d\'Ivoire',
      aplha3: 'CIV',
      id: 'CI',
      currencySymbol: 'Fr'
    },
    {aplha3: 'DNK', currencyId: 'DKK', currencyName: 'Danish krone', currencySymbol: 'kr', id: 'DK', countryName: 'Denmark'},
    {aplha3: 'EGY', currencyId: 'EGP', currencyName: 'Egyptian pound', currencySymbol: '£', id: 'EG', countryName: 'Egypt'},
    {
      currencyId: 'ETB',
      currencyName: 'Ethiopian birr',
      countryName: 'Ethiopia',
      aplha3: 'ETH',
      id: 'ET',
      currencySymbol: 'Br'
    },
    {
      currencyId: 'XAF',
      currencyName: 'Central African CFA franc',
      countryName: 'Gabon',
      aplha3: 'GAB',
      id: 'GA',
      currencySymbol: 'Fr'
    },
    {
      aplha3: 'GIB',
      currencyId: 'GIP',
      currencyName: 'Gibraltar pound',
      currencySymbol: '£',
      id: 'GI',
      countryName: 'Gibraltar'
    },
    {
      currencyId: 'XOF',
      currencyName: 'West African CFA franc',
      countryName: 'Guinea-Bissau',
      aplha3: 'GNB',
      id: 'GW',
      currencySymbol: 'Fr'
    },
    {
      aplha3: 'HUN',
      currencyId: 'HUF',
      currencyName: 'Hungarian forint',
      currencySymbol: 'Ft',
      id: 'HU',
      countryName: 'Hungary'
    },
    {currencyId: 'IQD', currencyName: 'Iraqi dinar', countryName: 'Iraq', aplha3: 'IRQ', id: 'IQ', currencySymbol: 'ع.د'},
    {aplha3: 'JPN', currencyId: 'JPY', currencyName: 'Japanese yen', currencySymbol: '¥', id: 'JP', countryName: 'Japan'},
    {
      aplha3: 'PRK',
      currencyId: 'KPW',
      currencyName: 'North Korean won',
      currencySymbol: '₩',
      id: 'KP',
      countryName: 'Korea North'
    },
    {aplha3: 'LVA', currencyId: 'LVL', currencyName: 'Latvian lats', currencySymbol: 'Ls', id: 'LV', countryName: 'Latvia'},
    {
      aplha3: 'LIE',
      currencyId: 'CHF',
      currencyName: 'Swiss Franc',
      currencySymbol: 'Fr.',
      id: 'LI',
      countryName: 'Liechtenstein'
    },
    {
      currencyId: 'MGA',
      currencyName: 'Malagasy ariary',
      countryName: 'Madagascar',
      aplha3: 'MDG',
      id: 'MG',
      currencySymbol: 'Ar'
    },
    {aplha3: 'MLT', currencyId: 'EUR', currencyName: 'European Euro', currencySymbol: '€', id: 'MT', countryName: 'Malta'},
    {currencyId: 'MDL', currencyName: 'Moldovan leu', countryName: 'Moldova', aplha3: 'MDA', id: 'MD', currencySymbol: 'L'},
    {
      currencyId: 'MAD',
      currencyName: 'Moroccan dirham',
      countryName: 'Morocco',
      aplha3: 'MAR',
      id: 'MA',
      currencySymbol: 'د.م.'
    },
    {aplha3: 'NPL', currencyId: 'NPR', currencyName: 'Nepalese rupee', currencySymbol: '₨', id: 'NP', countryName: 'Nepal'},
    {
      currencyId: 'XOF',
      currencyName: 'West African CFA franc',
      countryName: 'Niger',
      aplha3: 'NER',
      id: 'NE',
      currencySymbol: 'Fr'
    },
    {aplha3: 'PLW', currencyId: 'USD', currencyName: 'U.S. Dollar', currencySymbol: '$', id: 'PW', countryName: 'Palau'},
    {
      aplha3: 'PHL',
      currencyId: 'PHP',
      currencyName: 'Philippine peso',
      currencySymbol: '₱',
      id: 'PH',
      countryName: 'Philippines'
    },
    {
      aplha3: 'ROU',
      currencyId: 'RON',
      currencyName: 'Romanian leu',
      currencySymbol: 'lei',
      id: 'RO',
      countryName: 'Romania'
    },
    {
      aplha3: 'LCA',
      currencyId: 'XCD',
      currencyName: 'East Caribbean dollar',
      currencySymbol: '$',
      id: 'LC',
      countryName: 'Saint Lucia'
    },
    {
      aplha3: 'SAU',
      currencyId: 'SAR',
      currencyName: 'Saudi riyal',
      currencySymbol: '﷼',
      id: 'SA',
      countryName: 'Saudi Arabia'
    },
    {
      aplha3: 'SGP',
      currencyId: 'SGD',
      currencyName: 'Singapore dollar',
      currencySymbol: '$',
      id: 'SG',
      countryName: 'Singapore'
    },
    {
      aplha3: 'ZAF',
      currencyId: 'ZAR',
      currencyName: 'South African rand',
      currencySymbol: 'R',
      id: 'ZA',
      countryName: 'South Africa'
    },
    {
      aplha3: 'SUR',
      currencyId: 'SRD',
      currencyName: 'SuricountryNamese dollar',
      currencySymbol: '$',
      id: 'SR',
      countryName: 'SuricountryName'
    },
    {
      aplha3: 'TWN',
      currencyId: 'TWD',
      currencyName: 'New Taiwan dollar',
      currencySymbol: 'NT$',
      id: 'TW',
      countryName: 'Taiwan'
    },
    {currencyId: 'TOP', currencyName: 'Paanga', countryName: 'Tonga', aplha3: 'TON', id: 'TO', currencySymbol: 'T$'},
    {
      aplha3: 'TUV',
      currencyId: 'AUD',
      currencyName: 'Australian dollar',
      currencySymbol: '$',
      id: 'TV',
      countryName: 'Tuvalu'
    },
    {
      aplha3: 'USA',
      currencyId: 'USD',
      currencyName: 'United States dollar',
      currencySymbol: '$',
      id: 'US',
      countryName: 'United States of America'
    },
    {
      aplha3: 'VNM',
      currencyId: 'VND',
      currencyName: 'VietcountryNamese dong',
      currencySymbol: '₫',
      id: 'VN',
      countryName: 'Vietnam'
    },
    {
      aplha3: 'ALB',
      currencyId: 'ALL',
      currencyName: 'Albanian lek',
      currencySymbol: 'Lek',
      id: 'AL',
      countryName: 'Albania'
    },
    {
      aplha3: 'ATG',
      currencyId: 'XCD',
      currencyName: 'East Caribbean dollar',
      currencySymbol: '$',
      id: 'AG',
      countryName: 'Antigua and Barbuda'
    },
    {aplha3: 'AUT', currencyId: 'EUR', currencyName: 'European euro', currencySymbol: '€', id: 'AT', countryName: 'Austria'},
    {
      aplha3: 'BRB',
      currencyId: 'BBD',
      currencyName: 'Barbadian dollar',
      currencySymbol: '$',
      id: 'BB',
      countryName: 'Barbados'
    },
    {
      currencyId: 'BTN',
      currencyName: 'Bhutanese ngultrum',
      countryName: 'Bhutan',
      aplha3: 'BTN',
      id: 'BT',
      currencySymbol: 'Nu.'
    },
    {aplha3: 'BRN', currencyId: 'BND', currencyName: 'Brunei dollar', currencySymbol: '$', id: 'BN', countryName: 'Brunei'},
    {
      currencyId: 'XAF',
      currencyName: 'Central African CFA franc',
      countryName: 'Cameroon',
      aplha3: 'CMR',
      id: 'CM',
      currencySymbol: 'Fr'
    },
    {aplha3: 'CHL', currencyId: 'CLP', currencyName: 'Chilean peso', currencySymbol: '$', id: 'CL', countryName: 'Chile'},
    {
      currencyId: 'CDF',
      currencyName: 'Congolese franc',
      countryName: 'Congo, Democratic Republic',
      aplha3: 'COD',
      id: 'CD',
      currencySymbol: 'Fr'
    },
    {aplha3: 'CYP', currencyId: 'EUR', currencyName: 'European euro', currencySymbol: '€', id: 'CY', countryName: 'Cyprus'},
    {
      aplha3: 'DOM',
      currencyId: 'DOP',
      currencyName: 'Dominican peso',
      currencySymbol: 'RD$',
      id: 'DO',
      countryName: 'Dominican Republic'
    },
    {
      currencyId: 'ERN',
      currencyName: 'Eritrean nakfa',
      countryName: 'Eritrea',
      aplha3: 'ERI',
      id: 'ER',
      currencySymbol: 'Nfk'
    },
    {aplha3: 'FRA', currencyId: 'EUR', currencyName: 'European euro', currencySymbol: '€', id: 'FR', countryName: 'France'},
    {aplha3: 'DEU', currencyId: 'EUR', currencyName: 'European euro', currencySymbol: '€', id: 'DE', countryName: 'Germany'},
    {
      aplha3: 'GTM',
      currencyId: 'GTQ',
      currencyName: 'Guatemalan quetzal',
      currencySymbol: 'Q',
      id: 'GT',
      countryName: 'Guatemala'
    },
    {
      aplha3: 'HND',
      currencyId: 'HNL',
      currencyName: 'Honduran lempira',
      currencySymbol: 'L',
      id: 'HN',
      countryName: 'Honduras'
    },
    {
      aplha3: 'IDN',
      currencyId: 'IDR',
      currencyName: 'Indonesian rupiah',
      currencySymbol: 'Rp',
      id: 'ID',
      countryName: 'Indonesia'
    },
    {aplha3: 'ITA', currencyId: 'EUR', currencyName: 'European euro', currencySymbol: '€', id: 'IT', countryName: 'Italy'},
    {
      aplha3: 'KEN',
      currencyId: 'KES',
      currencyName: 'Kenyan shilling',
      currencySymbol: 'KSh',
      id: 'KE',
      countryName: 'Kenya'
    },
    {
      aplha3: 'KGZ',
      currencyId: 'KGS',
      currencyName: 'Kyrgyzstani som',
      currencySymbol: 'лв',
      id: 'KG',
      countryName: 'Kyrgyzstan'
    },
    {
      aplha3: 'LBR',
      currencyId: 'LRD',
      currencyName: 'Liberian dollar',
      currencySymbol: '$',
      id: 'LR',
      countryName: 'Liberia'
    },
    {currencyId: 'MOP', currencyName: 'Macanese pataca', countryName: 'Macau', aplha3: 'MAC', id: 'MO', currencySymbol: 'P'},
    {
      currencyId: 'MVR',
      currencyName: 'Maldivian rufiyaa',
      countryName: 'Maldives',
      aplha3: 'MDV',
      id: 'MV',
      currencySymbol: '.ރ'
    },
    {aplha3: 'MEX', currencyId: 'MXN', currencyName: 'Mexican peso', currencySymbol: '$', id: 'MX', countryName: 'Mexico'},
    {
      aplha3: 'MNE',
      currencyId: 'EUR',
      currencyName: 'European Euro',
      currencySymbol: '€',
      id: 'ME',
      countryName: 'Montenegro'
    },
    {
      aplha3: 'NAM',
      currencyId: 'NAD',
      currencyName: 'Namibian dollar',
      currencySymbol: '$',
      id: 'NA',
      countryName: 'Namibia'
    },
    {
      aplha3: 'NZL',
      currencyId: 'NZD',
      currencyName: 'New Zealand dollar',
      currencySymbol: '$',
      id: 'NZ',
      countryName: 'New Zealand'
    },
    {aplha3: 'OMN', currencyId: 'OMR', currencyName: 'Omani rial', currencySymbol: '﷼', id: 'OM', countryName: 'Oman'},
    {
      aplha3: 'PRY',
      currencyId: 'PYG',
      currencyName: 'Paraguayan guarani',
      currencySymbol: 'Gs',
      id: 'PY',
      countryName: 'Paraguay'
    },
    {
      aplha3: 'PRI',
      currencyId: 'USD',
      currencyName: 'U.S. Dollar',
      currencySymbol: '$',
      id: 'PR',
      countryName: 'Puerto Rico'
    },
    {
      aplha3: 'SHN',
      currencyId: 'SHP',
      currencyName: 'Saint Helena pound',
      currencySymbol: '£',
      id: 'SH',
      countryName: 'Saint Helena'
    },
    {
      aplha3: 'SMR',
      currencyId: 'EUR',
      currencyName: 'European euro',
      currencySymbol: '€',
      id: 'SM',
      countryName: 'San Marino'
    },
    {
      aplha3: 'SYC',
      currencyId: 'SCR',
      currencyName: 'Seychellois rupee',
      currencySymbol: '₨',
      id: 'SC',
      countryName: 'Seychelles'
    },
    {
      aplha3: 'SLB',
      currencyId: 'SBD',
      currencyName: 'Solomon Islands dollar',
      currencySymbol: '$',
      id: 'SB',
      countryName: 'Solomon Islands'
    },
    {
      aplha3: 'LKA',
      currencyId: 'LKR',
      currencyName: 'Sri Lankan rupee',
      currencySymbol: '₨',
      id: 'LK',
      countryName: 'Sri Lanka'
    },
    {
      aplha3: 'CHE',
      currencyId: 'CHF',
      currencyName: 'Swiss franc',
      currencySymbol: 'Fr.',
      id: 'CH',
      countryName: 'Switzerland'
    },
    {aplha3: 'THA', currencyId: 'THB', currencyName: 'Thai baht', currencySymbol: '฿', id: 'TH', countryName: 'Thailand'},
    {
      currencyId: 'TRY',
      currencyName: 'Turkish new lira',
      countryName: 'Turkey',
      aplha3: 'TUR',
      id: 'TR',
      currencySymbol: '₺'
    },
    {
      currencyId: 'AED',
      currencyName: 'UAE dirham',
      countryName: 'United Arab Emirates',
      aplha3: 'ARE',
      id: 'AE',
      currencySymbol: 'فلس'
    },
    {currencyId: 'VUV', currencyName: 'Vanuatu vatu', countryName: 'Vanuatu', aplha3: 'VUT', id: 'VU', currencySymbol: 'Vt'},
    {
      currencyId: 'ZMW',
      currencyName: 'Zambian kwacha',
      countryName: 'Zambia',
      aplha3: 'ZMB',
      id: 'ZM',
      currencySymbol: 'ZK'
    },
    {
      currencyId: 'AOA',
      currencyName: 'Angolan kwanza',
      countryName: 'Angola',
      aplha3: 'AGO',
      id: 'AO',
      currencySymbol: 'Kz'
    },
    {aplha3: 'ABW', currencyId: 'AWG', currencyName: 'Aruban florin', currencySymbol: 'ƒ', id: 'AW', countryName: 'Aruba'},
    {
      currencyId: 'BHD',
      currencyName: 'Bahraini dinar',
      countryName: 'Bahrain',
      aplha3: 'BHR',
      id: 'BH',
      currencySymbol: 'دينار'
    },
    {
      aplha3: 'BLZ',
      currencyId: 'BZD',
      currencyName: 'Belize dollar',
      currencySymbol: 'BZ$',
      id: 'BZ',
      countryName: 'Belize'
    },
    {
      aplha3: 'BWA',
      currencyId: 'BWP',
      currencyName: 'Botswana pula',
      currencySymbol: 'P',
      id: 'BW',
      countryName: 'Botswana'
    },
    {
      currencyId: 'BIF',
      currencyName: 'Burundi franc',
      countryName: 'Burundi',
      aplha3: 'BDI',
      id: 'BI',
      currencySymbol: 'Fr'
    },
    {
      currencyId: 'XAF',
      currencyName: 'Central African CFA franc',
      countryName: 'Central African Republic',
      aplha3: 'CAF',
      id: 'CF',
      currencySymbol: 'Fr'
    },
    {
      currencyId: 'KMF',
      currencyName: 'Comorian franc',
      countryName: 'Comoros',
      aplha3: 'COM',
      id: 'KM',
      currencySymbol: 'Fr'
    },
    {
      aplha3: 'HRV',
      currencyId: 'HRK',
      currencyName: 'Croatian kuna',
      currencySymbol: 'kn',
      id: 'HR',
      countryName: 'Croatia'
    },
    {
      currencyId: 'DJF',
      currencyName: 'Djiboutian franc',
      countryName: 'Djibouti',
      aplha3: 'DJI',
      id: 'DJ',
      currencySymbol: 'Fr'
    },
    {
      aplha3: 'SLV',
      currencyId: 'USD',
      currencyName: 'U.S. Dollar',
      currencySymbol: '$',
      id: 'SV',
      countryName: 'El Salvador'
    },
    {aplha3: 'FJI', currencyId: 'FJD', currencyName: 'Fijian dollar', currencySymbol: '$', id: 'FJ', countryName: 'Fiji'},
    {currencyId: 'GMD', currencyName: 'Gambian dalasi', countryName: 'Gambia', aplha3: 'GMB', id: 'GM', currencySymbol: 'D'},
    {aplha3: 'GRC', currencyId: 'EUR', currencyName: 'European euro', currencySymbol: '€', id: 'GR', countryName: 'Greece'},
    {
      aplha3: 'GUY',
      currencyId: 'GYD',
      currencyName: 'Guyanese dollar',
      currencySymbol: '$',
      id: 'GY',
      countryName: 'Guyana'
    },
    {
      aplha3: 'ISL',
      currencyId: 'ISK',
      currencyName: 'Icelandic króna',
      currencySymbol: 'kr',
      id: 'IS',
      countryName: 'Iceland'
    },
    {aplha3: 'IRL', currencyId: 'EUR', currencyName: 'European euro', currencySymbol: '€', id: 'IE', countryName: 'Ireland'},
    {
      currencyId: 'JOD',
      currencyName: 'Jordanian dinar',
      countryName: 'Jordan',
      aplha3: 'JOR',
      id: 'JO',
      currencySymbol: 'د.ا '
    },
    {
      aplha3: 'KOR',
      currencyId: 'KRW',
      currencyName: 'South Korean won',
      currencySymbol: '₩',
      id: 'KR',
      countryName: 'Korea South'
    },
    {aplha3: 'LBN', currencyId: 'LBP', currencyName: 'Lebanese lira', currencySymbol: '£', id: 'LB', countryName: 'Lebanon'},
    {
      currencyId: 'MWK',
      currencyName: 'Malawian kwacha',
      countryName: 'Malawi',
      aplha3: 'MWI',
      id: 'MW',
      currencySymbol: 'MK'
    },
    {
      currencyId: 'MRO',
      currencyName: 'Mauritanian ouguiya',
      countryName: 'Mauritania',
      aplha3: 'MRT',
      id: 'MR',
      currencySymbol: 'UM'
    },
    {aplha3: 'MCO', currencyId: 'EUR', currencyName: 'European Euro', currencySymbol: '€', id: 'MC', countryName: 'Monaco'},
    {
      currencyId: 'MZN',
      currencyName: 'Mozambican metical',
      countryName: 'Mozambique',
      aplha3: 'MOZ',
      id: 'MZ',
      currencySymbol: 'MT'
    },
    {
      aplha3: 'NLD',
      currencyId: 'EUR',
      currencyName: 'European euro',
      currencySymbol: '€',
      id: 'NL',
      countryName: 'Netherlands'
    },
    {
      aplha3: 'NGA',
      currencyId: 'NGN',
      currencyName: 'Nigerian naira',
      currencySymbol: '₦',
      id: 'NG',
      countryName: 'Nigeria'
    },
    {
      aplha3: 'PAN',
      currencyId: 'PAB',
      currencyName: 'Panamanian balboa',
      currencySymbol: 'B/.',
      id: 'PA',
      countryName: 'Panama'
    },
    {aplha3: 'POL', currencyId: 'PLN', currencyName: 'Polish zloty', currencySymbol: 'zł', id: 'PL', countryName: 'Poland'},
    {
      aplha3: 'RUS',
      currencyId: 'RUB',
      currencyName: 'Russian ruble',
      currencySymbol: 'руб',
      id: 'RU',
      countryName: 'Russia'
    },
    {
      aplha3: 'VCT',
      currencyId: 'XCD',
      currencyName: 'East Caribbean dollar',
      currencySymbol: '$',
      id: 'VC',
      countryName: 'Saint Vincent and the Grenadines'
    },
    {
      currencyId: 'XOF',
      currencyName: 'West African CFA franc',
      countryName: 'Senegal',
      aplha3: 'SEN',
      id: 'SN',
      currencySymbol: 'Fr'
    },
    {
      aplha3: 'SVK',
      currencyId: 'EUR',
      currencyName: 'European euro',
      currencySymbol: '€',
      id: 'SK',
      countryName: 'Slovakia'
    },
    {
      currencyId: 'SDG',
      currencyName: 'Sudanese pound',
      countryName: 'South Sudan',
      aplha3: 'SSD',
      id: 'SS',
      currencySymbol: '£'
    },
    {
      currencyId: 'SZL',
      currencyName: 'Swazi lilangeni',
      countryName: 'Swaziland',
      aplha3: 'SWZ',
      id: 'SZ',
      currencySymbol: 'L'
    },
    {
      currencyId: 'TJS',
      currencyName: 'Tajikistani somoni',
      countryName: 'Tajikistan',
      aplha3: 'TJK',
      id: 'TJ',
      currencySymbol: 'ЅМ'
    },
    {
      aplha3: 'TTO',
      currencyId: 'TTD',
      currencyName: 'Trinidad and Tobago dollar',
      currencySymbol: 'TT$',
      id: 'TT',
      countryName: 'Trinidad and Tobago'
    },
    {
      aplha3: 'UGA',
      currencyId: 'UGX',
      currencyName: 'Ugandan shilling',
      currencySymbol: 'USh',
      id: 'UG',
      countryName: 'Uganda'
    },
    {
      aplha3: 'URY',
      currencyId: 'UYU',
      currencyName: 'Uruguayan peso',
      currencySymbol: '$U',
      id: 'UY',
      countryName: 'Uruguay'
    },
    {
      currencyId: 'XPF',
      currencyName: 'CFP franc',
      countryName: 'Wallis and Futuna Islands',
      aplha3: 'WLF',
      id: 'WF',
      currencySymbol: 'Fr'
    },
    {
      aplha3: 'LTU',
      currencyId: 'EUR',
      currencyName: 'European euro',
      currencySymbol: '€',
      id: 'LT',
      countryName: 'Lithuania'
    }
  ];

  public static getList(): Currency [] {
    return this.theList;
  }

  public static lookupCountryCurrency(countryName: string) {
    let countryCurrency = null;
    for (let i = 0; i < this.theList.length; i++) {
      const theListElement = this.theList[i];

    }
    return
  }
}

