import { EventDayPipePipe } from './event-day-pipe.pipe';

describe('EventDayPipePipe', () => {

  // friday - February 5, 2021
  const startDate: Date = new Date(2021, 1, 5);

  // tuesday - July 6, 2021
  const usOpenStartDate: Date = new Date(2021, 6, 6);

  it('should be Friday', () => {
    const pipe = new EventDayPipePipe();
    expect(pipe.transform(1, startDate)).toEqual('Friday');
  });

  it('should be Saturday', () => {
    const pipe = new EventDayPipePipe();
    expect(pipe.transform(2, startDate)).toEqual('Saturday');
  });

  it('should be Sunday', () => {
    const pipe = new EventDayPipePipe();
    expect(pipe.transform(3, startDate)).toEqual('Sunday');
  });

  it('should be Monday', () => {
    const pipe = new EventDayPipePipe();
    expect(pipe.transform(4, startDate)).toEqual('Monday');
  });

  it('should be Tuesday', () => {
    const pipe = new EventDayPipePipe();
    expect(pipe.transform(1, usOpenStartDate)).toEqual('Tuesday');
  });

  it('should be Wednesday', () => {
    const pipe = new EventDayPipePipe();
    expect(pipe.transform(2, usOpenStartDate)).toEqual('Wednesday');
  });

  it('should be Thursday', () => {
    const pipe = new EventDayPipePipe();
    expect(pipe.transform(3, usOpenStartDate)).toEqual('Thursday');
  });

});
