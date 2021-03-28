import {inject, TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {CurrencyService} from './currency.service';

describe('CurrencyServiceService', () => {
  let httpTestingController: HttpTestingController;
  let currencyService: CurrencyService;
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });
    httpTestingController = TestBed.get(HttpTestingController);
  });

  beforeEach(inject(
    [CurrencyService],
    (service: CurrencyService) => {
      currencyService = service;
    }
  ));

  it('should be created', () => {
    expect(currencyService).toBeTruthy();
  });

  it('AAA convert from US dollar to Canadian Dollar', () => {
    let result = 0;
    const exchangeRate = 1.26;
    const usDollarAmount = 100;
    const expectedAmount = usDollarAmount * exchangeRate;
      currencyService.convertAmount(usDollarAmount, 'USD', 'CAD')
        .subscribe(
          (convertedAmount: number) => {
            result = convertedAmount;
          },
          (error: any) => {
            fail('unable to convert amount ' + JSON.stringify(error));
          });
    const req = httpTestingController.expectOne({
      method: 'GET',
      url: 'https://free.currconv.com/api/v7/convert?q=USD_CAD&compact=ultra&apiKey=4ddcbbbdbb60b519bdd8'
    });
    req.flush({USD_CAD: exchangeRate});

    expect(result).toEqual(expectedAmount);
    }
  );

  it('get exchange rate from US dollar to Canadian Dollar', () => {
    let result = 0;
    const exchangeRate = 1.26;
      currencyService.getExchangeRate( 'USD', 'CAD')
        .subscribe(
          (convertedAmount: number) => {
            result = convertedAmount;
          },
          (error: any) => {
            fail('unable to convert amount ' + JSON.stringify(error));
          });
    const req = httpTestingController.expectOne({
      method: 'GET',
      url: 'https://free.currconv.com/api/v7/convert?q=USD_CAD&compact=ultra&apiKey=4ddcbbbdbb60b519bdd8'
    });
    req.flush({USD_CAD: exchangeRate});

    expect(result).toEqual(exchangeRate);
    }
  );

    it('currency for USA to be USD', () => {
    const countryCurrency = currencyService.getCountryCurrencyId('US');
    expect(countryCurrency).toEqual('USD');
  });

  it('currency for Canada to be CAD', () => {
    const countryCurrency = currencyService.getCountryCurrencyId('CA');
    expect(countryCurrency).toEqual('CAD');
  });

});
