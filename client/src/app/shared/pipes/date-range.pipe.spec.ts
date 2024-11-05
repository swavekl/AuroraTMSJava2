import { DateRangePipe } from './date-range.pipe';
import moment from 'moment';

describe('DateRangePipe', () => {
  it('create an instance', () => {
    const pipe = new DateRangePipe();
    expect(pipe).toBeTruthy();
  });

  it('format 1 day event', () => {
    const pipe = new DateRangePipe();
    // from April 2 - 4
    const firstDay: Date = moment([2021, 3, 5]).toDate();
    const secondDay: Date = moment([2021, 3, 5]).toDate();
    const formattedRange = pipe.transform([firstDay, secondDay]);
    expect(formattedRange).toEqual('Apr 5, 2021');
  });

  it('format two days apart', () => {
    const pipe = new DateRangePipe();
    // from April 2 - 4
    const firstDay: Date = moment([2021, 3, 2]).toDate();
    const secondDay: Date = moment([2021, 3, 4]).toDate();
    const formattedRange = pipe.transform([firstDay, secondDay]);
    expect(formattedRange).toEqual('Apr 2 - 4, 2021');
  });

  it('format two days apart on month boundary', () => {
    const pipe = new DateRangePipe();
    // from April 2 - 4
    const firstDay: Date = moment([2021, 2, 31]).toDate();
    const secondDay: Date = moment([2021, 3, 3]).toDate();
    const formattedRange = pipe.transform([firstDay, secondDay]);
    expect(formattedRange).toEqual('Mar 31 - Apr 3, 2021');
  });

  it('format two days apart on year boundary', () => {
    const pipe = new DateRangePipe();
    // from April 2 - 4
    const firstDay: Date = moment([2021, 11, 30]).toDate();
    const secondDay: Date = moment([2022, 0, 3]).toDate();
    const formattedRange = pipe.transform([firstDay, secondDay]);
    expect(formattedRange).toEqual('Dec 30, 2021 - Jan 3, 2022');
  });
});
