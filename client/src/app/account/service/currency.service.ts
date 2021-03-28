import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import {switchMap} from 'rxjs/operators';
import {CurrenciesList, Currency} from '../../shared/currencies-list';
import {CountriesList, CountryDescriptor} from '../../shared/countries-list';

@Injectable({
  providedIn: 'root'
})
export class CurrencyService {

  constructor(private httpClient: HttpClient) {
  }

  getCountryCurrencyId(countryCode: string): string {
    let countryCurrency = null;
    const countriesList: CountryDescriptor [] = CountriesList.getList();
    for (let i = 0; i < countriesList.length; i++) {
      const countryDescriptor = countriesList[i];
      // 2 digit country code
      if (countryDescriptor.code === countryCode) {
        const countryName = countryDescriptor.name;
        const currenciesList: Currency [] = CurrenciesList.getList();
        for (let k = 0; k < currenciesList.length; k++) {
          const currency = currenciesList[k];
          if (currency.countryName === countryName) {
            countryCurrency = currency.currencyId;
            break;
          }
        }
        break;
      }
    }
    return countryCurrency;
  }

  /**
   * Converts from one currency to another
   * @param amount
   * @param fromCurrency
   * @param toCurrency
   */
  convertAmount(amount: number, fromCurrency: string, toCurrency: string): Observable<number> {
    // get currency exchange rate first
    return this.getExchangeRate(fromCurrency, toCurrency)
      .pipe(
        switchMap(
          (exchangeRate: number) => {
            // convert amount from one currency to another
            const convertedAmount = amount * exchangeRate;
            return of(convertedAmount);
          }
        )
      );
  }

  getExchangeRate(fromCurrency: string, toCurrency: string): Observable<number> {
    // get currency exchange rate
    const key = `${fromCurrency}_${toCurrency}`;
    const url = `/api/currency/exchangerate/${key}`;
    return this.httpClient.get(url)
      .pipe(
        switchMap(
          (response: any) => {
            console.log ('got echange rate response ' + JSON.stringify(response));
            let exchangeRate = 1; // i.e. no exchange rate
            if (response && response[key] != null) {
              exchangeRate = response[key];
            }
            return of (exchangeRate);
          }
        )
      );
  }
}


