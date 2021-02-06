import { StartTimePipe } from './start-time.pipe';

describe('StartTimePipe', () => {
  it('test early morning half hour', () => {
    const pipe = new StartTimePipe();
    expect(pipe.transform(9.5)).toEqual('9:30 AM');
  });
  it('test morning', () => {
    const pipe = new StartTimePipe();
    expect(pipe.transform(10)).toEqual('10:00 AM');
  });
  it('test afternoon', () => {
    const pipe = new StartTimePipe();
    expect(pipe.transform(17)).toEqual('5:00 PM');
  });
  it('test afternoon half hour', () => {
    const pipe = new StartTimePipe();
    expect(pipe.transform(14.5)).toEqual('2:30 PM');
  });
});
