import {OrderOfServingCalculator} from './order-of-serving-calculator';

describe('OrderOfServingCalculator', () => {
  // doubles
  let calculator = new OrderOfServingCalculator(5, true);
  calculator.recordServerAndReceiver('A', 'X');
  let lastDoublesGame = 1;
  it('Doubles initial server A', () => {
    expect(calculator.getServer()).toMatch('A');
  });
  it('Doubles initial receiver X', () => {
    expect(calculator.getReceiver()).toMatch('X');
  });

  test.each([
    {game: 1, pointsSideA: 0, pointsSideB: 0, expectedServer: "A", expectedReceiver: "X"},
    {game: 1, pointsSideA: 0, pointsSideB: 1, expectedServer: "A", expectedReceiver: "X"},
    {game: 1, pointsSideA: 1, pointsSideB: 1, expectedServer: "X", expectedReceiver: "B"},
    {game: 1, pointsSideA: 2, pointsSideB: 1, expectedServer: "X", expectedReceiver: "B"},
    {game: 1, pointsSideA: 3, pointsSideB: 1, expectedServer: "B", expectedReceiver: "Y"},
    {game: 1, pointsSideA: 3, pointsSideB: 2, expectedServer: "B", expectedReceiver: "Y"},
    {game: 1, pointsSideA: 3, pointsSideB: 3, expectedServer: "Y", expectedReceiver: "A"},
    {game: 1, pointsSideA: 3, pointsSideB: 4, expectedServer: "Y", expectedReceiver: "A"},
    {game: 1, pointsSideA: 3, pointsSideB: 5, expectedServer: "A", expectedReceiver: "X"},
    {game: 1, pointsSideA: 3, pointsSideB: 6, expectedServer: "A", expectedReceiver: "X"},
    {game: 1, pointsSideA: 4, pointsSideB: 6, expectedServer: "X", expectedReceiver: "B"},
    {game: 1, pointsSideA: 5, pointsSideB: 6, expectedServer: "X", expectedReceiver: "B"},
    {game: 1, pointsSideA: 6, pointsSideB: 6, expectedServer: "B", expectedReceiver: "Y"},
    {game: 1, pointsSideA: 6, pointsSideB: 7, expectedServer: "B", expectedReceiver: "Y"},
    {game: 1, pointsSideA: 6, pointsSideB: 8, expectedServer: "Y", expectedReceiver: "A"},
    {game: 1, pointsSideA: 6, pointsSideB: 9, expectedServer: "Y", expectedReceiver: "A"},
    {game: 1, pointsSideA: 6, pointsSideB: 10, expectedServer: "A", expectedReceiver: "X"},
    // 0 : 1
    // game 2
    {game: 2, pointsSideA: 0, pointsSideB: 0, expectedServer: "Y", expectedReceiver: "B"},
    {game: 2, pointsSideA: 1, pointsSideB: 0, expectedServer: "Y", expectedReceiver: "B"},
    {game: 2, pointsSideA: 2, pointsSideB: 0, expectedServer: "B", expectedReceiver: "X"},
    {game: 2, pointsSideA: 3, pointsSideB: 0, expectedServer: "B", expectedReceiver: "X"},
    {game: 2, pointsSideA: 3, pointsSideB: 1, expectedServer: "X", expectedReceiver: "A"},
    {game: 2, pointsSideA: 3, pointsSideB: 2, expectedServer: "X", expectedReceiver: "A"},
    {game: 2, pointsSideA: 3, pointsSideB: 3, expectedServer: "A", expectedReceiver: "Y"},
    {game: 2, pointsSideA: 3, pointsSideB: 4, expectedServer: "A", expectedReceiver: "Y"},
    {game: 2, pointsSideA: 4, pointsSideB: 4, expectedServer: "Y", expectedReceiver: "B"},
    {game: 2, pointsSideA: 5, pointsSideB: 4, expectedServer: "Y", expectedReceiver: "B"},
    {game: 2, pointsSideA: 5, pointsSideB: 5, expectedServer: "B", expectedReceiver: "X"},
    {game: 2, pointsSideA: 6, pointsSideB: 5, expectedServer: "B", expectedReceiver: "X"},
    {game: 2, pointsSideA: 7, pointsSideB: 5, expectedServer: "X", expectedReceiver: "A"},
    {game: 2, pointsSideA: 7, pointsSideB: 6, expectedServer: "X", expectedReceiver: "A"},
    {game: 2, pointsSideA: 8, pointsSideB: 6, expectedServer: "A", expectedReceiver: "Y"},
    {game: 2, pointsSideA: 8, pointsSideB: 7, expectedServer: "A", expectedReceiver: "Y"},
    {game: 2, pointsSideA: 8, pointsSideB: 8, expectedServer: "Y", expectedReceiver: "B"},
    {game: 2, pointsSideA: 9, pointsSideB: 8, expectedServer: "Y", expectedReceiver: "B"},
    {game: 2, pointsSideA: 9, pointsSideB: 9, expectedServer: "B", expectedReceiver: "X"},
    {game: 2, pointsSideA: 9, pointsSideB: 10, expectedServer: "B", expectedReceiver: "X"},
    {game: 2, pointsSideA: 10, pointsSideB: 10, expectedServer: "X", expectedReceiver: "A"},
    {game: 2, pointsSideA: 11, pointsSideB: 10, expectedServer: "A", expectedReceiver: "Y"},
    {game: 2, pointsSideA: 11, pointsSideB: 11, expectedServer: "Y", expectedReceiver: "B"},
    {game: 2, pointsSideA: 12, pointsSideB: 11, expectedServer: "B", expectedReceiver: "X"},
    // 1 : 1
    // game 3
    {game: 3, pointsSideA: 0, pointsSideB: 0, expectedServer: "B", expectedReceiver: "Y"},
    {game: 3, pointsSideA: 0, pointsSideB: 1, expectedServer: "B", expectedReceiver: "Y"},
    {game: 3, pointsSideA: 1, pointsSideB: 1, expectedServer: "Y", expectedReceiver: "A"},
    {game: 3, pointsSideA: 1, pointsSideB: 2, expectedServer: "Y", expectedReceiver: "A"},
    {game: 3, pointsSideA: 2, pointsSideB: 2, expectedServer: "A", expectedReceiver: "X"},
    {game: 3, pointsSideA: 3, pointsSideB: 2, expectedServer: "A", expectedReceiver: "X"},
    {game: 3, pointsSideA: 4, pointsSideB: 2, expectedServer: "X", expectedReceiver: "B"},
    {game: 3, pointsSideA: 5, pointsSideB: 2, expectedServer: "X", expectedReceiver: "B"},
    {game: 3, pointsSideA: 6, pointsSideB: 2, expectedServer: "B", expectedReceiver: "Y"},
    {game: 3, pointsSideA: 7, pointsSideB: 2, expectedServer: "B", expectedReceiver: "Y"},
    {game: 3, pointsSideA: 7, pointsSideB: 3, expectedServer: "Y", expectedReceiver: "A"},
    {game: 3, pointsSideA: 8, pointsSideB: 3, expectedServer: "Y", expectedReceiver: "A"},
    {game: 3, pointsSideA: 8, pointsSideB: 4, expectedServer: "A", expectedReceiver: "X"},
    {game: 3, pointsSideA: 9, pointsSideB: 4, expectedServer: "A", expectedReceiver: "X"},
    {game: 3, pointsSideA: 10, pointsSideB: 4, expectedServer: "X", expectedReceiver: "B"},
    {game: 3, pointsSideA: 10, pointsSideB: 5, expectedServer: "X", expectedReceiver: "B"},
    // 2 : 1
    // game 3
    {game: 4, pointsSideA: 0, pointsSideB: 0, expectedServer: "X", expectedReceiver: "A"},
    {game: 4, pointsSideA: 0, pointsSideB: 1, expectedServer: "X", expectedReceiver: "A"},
    {game: 4, pointsSideA: 1, pointsSideB: 1, expectedServer: "A", expectedReceiver: "Y"},
    {game: 4, pointsSideA: 1, pointsSideB: 2, expectedServer: "A", expectedReceiver: "Y"},
    {game: 4, pointsSideA: 2, pointsSideB: 2, expectedServer: "Y", expectedReceiver: "B"},
    {game: 4, pointsSideA: 3, pointsSideB: 2, expectedServer: "Y", expectedReceiver: "B"},
    {game: 4, pointsSideA: 4, pointsSideB: 2, expectedServer: "B", expectedReceiver: "X"},
    {game: 4, pointsSideA: 5, pointsSideB: 2, expectedServer: "B", expectedReceiver: "X"},
    {game: 4, pointsSideA: 6, pointsSideB: 2, expectedServer: "X", expectedReceiver: "A"},
    {game: 4, pointsSideA: 7, pointsSideB: 2, expectedServer: "X", expectedReceiver: "A"},
    {game: 4, pointsSideA: 7, pointsSideB: 3, expectedServer: "A", expectedReceiver: "Y"},
    {game: 4, pointsSideA: 8, pointsSideB: 3, expectedServer: "A", expectedReceiver: "Y"},
    {game: 4, pointsSideA: 8, pointsSideB: 4, expectedServer: "Y", expectedReceiver: "B"},
    {game: 4, pointsSideA: 8, pointsSideB: 5, expectedServer: "Y", expectedReceiver: "B"},
    {game: 4, pointsSideA: 8, pointsSideB: 6, expectedServer: "B", expectedReceiver: "X"},
    {game: 4, pointsSideA: 8, pointsSideB: 7, expectedServer: "B", expectedReceiver: "X"},
    {game: 4, pointsSideA: 8, pointsSideB: 8, expectedServer: "X", expectedReceiver: "A"},
    {game: 4, pointsSideA: 8, pointsSideB: 9, expectedServer: "X", expectedReceiver: "A"},
    {game: 4, pointsSideA: 8, pointsSideB: 10, expectedServer: "A", expectedReceiver: "Y"},
    {game: 4, pointsSideA: 9, pointsSideB: 10, expectedServer: "A", expectedReceiver: "Y"},
    // 2 : 2
    // game 5
    {game: 5, pointsSideA: 0, pointsSideB: 0, expectedServer: "B", expectedReceiver: "Y"},
    {game: 5, pointsSideA: 0, pointsSideB: 1, expectedServer: "B", expectedReceiver: "Y"},
    {game: 5, pointsSideA: 1, pointsSideB: 1, expectedServer: "Y", expectedReceiver: "A"},
    {game: 5, pointsSideA: 1, pointsSideB: 2, expectedServer: "Y", expectedReceiver: "A"},
    {game: 5, pointsSideA: 2, pointsSideB: 2, expectedServer: "A", expectedReceiver: "X"},
    {game: 5, pointsSideA: 3, pointsSideB: 2, expectedServer: "A", expectedReceiver: "X"},
    {game: 5, pointsSideA: 4, pointsSideB: 2, expectedServer: "X", expectedReceiver: "B"},
    // change at 5 points in last game
    {game: 5, pointsSideA: 5, pointsSideB: 2, expectedServer: "X", expectedReceiver: "A"},
    {game: 5, pointsSideA: 6, pointsSideB: 2, expectedServer: "A", expectedReceiver: "Y"},
    {game: 5, pointsSideA: 7, pointsSideB: 2, expectedServer: "A", expectedReceiver: "Y"},
    {game: 5, pointsSideA: 7, pointsSideB: 3, expectedServer: "Y", expectedReceiver: "B"},
    {game: 5, pointsSideA: 8, pointsSideB: 3, expectedServer: "Y", expectedReceiver: "B"},
    {game: 5, pointsSideA: 8, pointsSideB: 4, expectedServer: "B", expectedReceiver: "X"},
    {game: 5, pointsSideA: 9, pointsSideB: 4, expectedServer: "B", expectedReceiver: "X"},
    {game: 5, pointsSideA: 10, pointsSideB: 4, expectedServer: "X", expectedReceiver: "A"},
    {game: 5, pointsSideA: 10, pointsSideB: 5, expectedServer: "X", expectedReceiver: "A"},
    {game: 5, pointsSideA: 10, pointsSideB: 6, expectedServer: "A", expectedReceiver: "Y"},


  ])('Doubles game $game at ($pointsSideA, $pointsSideB) -> expected server $expectedServer, expected receiver $expectedReceiver',
    ({game, pointsSideA, pointsSideB, expectedServer, expectedReceiver}) => {
      if (lastDoublesGame != game) {
        lastDoublesGame = game;
        const nextGameIndex = calculator.startNextGame();
        let nextReceiver = null;
        switch (nextGameIndex) {
          case 1:
            nextReceiver = calculator.determineNextReceiver('Y');
            expect(nextReceiver).toMatch('B');
            calculator.recordServerAndReceiver('Y', nextReceiver);
            break;
          case 2:
            nextReceiver = calculator.determineNextReceiver('B');
            expect(nextReceiver).toMatch('Y');
            calculator.recordServerAndReceiver('B', nextReceiver);
            break;
          case 3:
            nextReceiver = calculator.determineNextReceiver('X');
            expect(nextReceiver).toMatch('A');
            calculator.recordServerAndReceiver('X', nextReceiver);
            break;
          case 4:
            nextReceiver = calculator.determineNextReceiver('B');
            expect(nextReceiver).toMatch('Y');
            calculator.recordServerAndReceiver('B', nextReceiver);
            break;
        }
      }

      calculator.determineNextServerAndReceiver(pointsSideA, pointsSideB);
    expect(calculator.getServer()).toMatch(expectedServer);
    expect(calculator.getReceiver()).toMatch(expectedReceiver);
  });


  let singlesCalculator = new OrderOfServingCalculator(5, false);
  singlesCalculator.recordServerAndReceiver('A', 'X');
  let lastSinglesGame = 1;
  it('Singles initial server A', () => {
    expect(singlesCalculator.getServer()).toMatch('A');
  });
  it('Singles initial receiver X', () => {
    expect(singlesCalculator.getReceiver()).toMatch('X');
  });

  test.each([
    {game: 1, pointsSideA: 0, pointsSideB: 0, expectedServer: "A", expectedReceiver: "X"},
    {game: 1, pointsSideA: 0, pointsSideB: 1, expectedServer: "A", expectedReceiver: "X"},
    {game: 1, pointsSideA: 1, pointsSideB: 1, expectedServer: "X", expectedReceiver: "A"},
    {game: 1, pointsSideA: 2, pointsSideB: 1, expectedServer: "X", expectedReceiver: "A"},
    {game: 1, pointsSideA: 3, pointsSideB: 1, expectedServer: "A", expectedReceiver: "X"},
    {game: 1, pointsSideA: 3, pointsSideB: 2, expectedServer: "A", expectedReceiver: "X"},
    {game: 1, pointsSideA: 3, pointsSideB: 3, expectedServer: "X", expectedReceiver: "A"},
    {game: 1, pointsSideA: 3, pointsSideB: 4, expectedServer: "X", expectedReceiver: "A"},
    {game: 1, pointsSideA: 3, pointsSideB: 5, expectedServer: "A", expectedReceiver: "X"},
    {game: 1, pointsSideA: 3, pointsSideB: 6, expectedServer: "A", expectedReceiver: "X"},
    {game: 1, pointsSideA: 4, pointsSideB: 6, expectedServer: "X", expectedReceiver: "A"},
    {game: 1, pointsSideA: 5, pointsSideB: 6, expectedServer: "X", expectedReceiver: "A"},
    {game: 1, pointsSideA: 6, pointsSideB: 6, expectedServer: "A", expectedReceiver: "X"},
    {game: 1, pointsSideA: 6, pointsSideB: 7, expectedServer: "A", expectedReceiver: "X"},
    {game: 1, pointsSideA: 6, pointsSideB: 8, expectedServer: "X", expectedReceiver: "A"},
    {game: 1, pointsSideA: 6, pointsSideB: 9, expectedServer: "X", expectedReceiver: "A"},
    {game: 1, pointsSideA: 6, pointsSideB: 10, expectedServer: "A", expectedReceiver: "X"},
    // 0 : 1
    // game 2
    {game: 2, pointsSideA: 0, pointsSideB: 0, expectedServer: "X", expectedReceiver: "A"},
    {game: 2, pointsSideA: 1, pointsSideB: 0, expectedServer: "X", expectedReceiver: "A"},
    {game: 2, pointsSideA: 2, pointsSideB: 0, expectedServer: "A", expectedReceiver: "X"},
    {game: 2, pointsSideA: 3, pointsSideB: 0, expectedServer: "A", expectedReceiver: "X"},
    {game: 2, pointsSideA: 3, pointsSideB: 1, expectedServer: "X", expectedReceiver: "A"},
    {game: 2, pointsSideA: 3, pointsSideB: 2, expectedServer: "X", expectedReceiver: "A"},
    {game: 2, pointsSideA: 3, pointsSideB: 3, expectedServer: "A", expectedReceiver: "X"},
    {game: 2, pointsSideA: 3, pointsSideB: 4, expectedServer: "A", expectedReceiver: "X"},
    {game: 2, pointsSideA: 4, pointsSideB: 4, expectedServer: "X", expectedReceiver: "A"},
    {game: 2, pointsSideA: 5, pointsSideB: 4, expectedServer: "X", expectedReceiver: "A"},
    {game: 2, pointsSideA: 5, pointsSideB: 5, expectedServer: "A", expectedReceiver: "X"},
    {game: 2, pointsSideA: 6, pointsSideB: 5, expectedServer: "A", expectedReceiver: "X"},
    {game: 2, pointsSideA: 7, pointsSideB: 5, expectedServer: "X", expectedReceiver: "A"},
    {game: 2, pointsSideA: 7, pointsSideB: 6, expectedServer: "X", expectedReceiver: "A"},
    {game: 2, pointsSideA: 8, pointsSideB: 6, expectedServer: "A", expectedReceiver: "X"},
    {game: 2, pointsSideA: 8, pointsSideB: 7, expectedServer: "A", expectedReceiver: "X"},
    {game: 2, pointsSideA: 8, pointsSideB: 8, expectedServer: "X", expectedReceiver: "A"},
    {game: 2, pointsSideA: 9, pointsSideB: 8, expectedServer: "X", expectedReceiver: "A"},
    {game: 2, pointsSideA: 9, pointsSideB: 9, expectedServer: "A", expectedReceiver: "X"},
    {game: 2, pointsSideA: 9, pointsSideB: 10, expectedServer: "A", expectedReceiver: "X"},
    {game: 2, pointsSideA: 10, pointsSideB: 10, expectedServer: "X", expectedReceiver: "A"},
    {game: 2, pointsSideA: 11, pointsSideB: 10, expectedServer: "A", expectedReceiver: "X"},
    {game: 2, pointsSideA: 11, pointsSideB: 11, expectedServer: "X", expectedReceiver: "A"},
    {game: 2, pointsSideA: 12, pointsSideB: 11, expectedServer: "A", expectedReceiver: "X"},
    // 1 : 1
    // game 3
    {game: 3, pointsSideA: 0, pointsSideB: 0, expectedServer: "A", expectedReceiver: "X"},
    {game: 3, pointsSideA: 0, pointsSideB: 1, expectedServer: "A", expectedReceiver: "X"},
    {game: 3, pointsSideA: 1, pointsSideB: 1, expectedServer: "X", expectedReceiver: "A"},
    {game: 3, pointsSideA: 1, pointsSideB: 2, expectedServer: "X", expectedReceiver: "A"},
    {game: 3, pointsSideA: 2, pointsSideB: 2, expectedServer: "A", expectedReceiver: "X"},
    {game: 3, pointsSideA: 3, pointsSideB: 2, expectedServer: "A", expectedReceiver: "X"},
    {game: 3, pointsSideA: 4, pointsSideB: 2, expectedServer: "X", expectedReceiver: "A"},
    {game: 3, pointsSideA: 5, pointsSideB: 2, expectedServer: "X", expectedReceiver: "A"},
    {game: 3, pointsSideA: 6, pointsSideB: 2, expectedServer: "A", expectedReceiver: "X"},
    {game: 3, pointsSideA: 7, pointsSideB: 2, expectedServer: "A", expectedReceiver: "X"},
    {game: 3, pointsSideA: 7, pointsSideB: 3, expectedServer: "X", expectedReceiver: "A"},
    {game: 3, pointsSideA: 8, pointsSideB: 3, expectedServer: "X", expectedReceiver: "A"},
    {game: 3, pointsSideA: 8, pointsSideB: 4, expectedServer: "A", expectedReceiver: "X"},
    {game: 3, pointsSideA: 9, pointsSideB: 4, expectedServer: "A", expectedReceiver: "X"},
    {game: 3, pointsSideA: 10, pointsSideB: 4, expectedServer: "X", expectedReceiver: "A"},
    {game: 3, pointsSideA: 10, pointsSideB: 5, expectedServer: "X", expectedReceiver: "A"},
    // 2 : 1
    // game 3
    {game: 4, pointsSideA: 0, pointsSideB: 0, expectedServer: "X", expectedReceiver: "A"},
    {game: 4, pointsSideA: 0, pointsSideB: 1, expectedServer: "X", expectedReceiver: "A"},
    {game: 4, pointsSideA: 1, pointsSideB: 1, expectedServer: "A", expectedReceiver: "X"},
    {game: 4, pointsSideA: 1, pointsSideB: 2, expectedServer: "A", expectedReceiver: "X"},
    {game: 4, pointsSideA: 2, pointsSideB: 2, expectedServer: "X", expectedReceiver: "A"},
    {game: 4, pointsSideA: 3, pointsSideB: 2, expectedServer: "X", expectedReceiver: "A"},
    {game: 4, pointsSideA: 4, pointsSideB: 2, expectedServer: "A", expectedReceiver: "X"},
    {game: 4, pointsSideA: 5, pointsSideB: 2, expectedServer: "A", expectedReceiver: "X"},
    {game: 4, pointsSideA: 6, pointsSideB: 2, expectedServer: "X", expectedReceiver: "A"},
    {game: 4, pointsSideA: 7, pointsSideB: 2, expectedServer: "X", expectedReceiver: "A"},
    {game: 4, pointsSideA: 7, pointsSideB: 3, expectedServer: "A", expectedReceiver: "X"},
    {game: 4, pointsSideA: 8, pointsSideB: 3, expectedServer: "A", expectedReceiver: "X"},
    {game: 4, pointsSideA: 8, pointsSideB: 4, expectedServer: "X", expectedReceiver: "A"},
    {game: 4, pointsSideA: 8, pointsSideB: 5, expectedServer: "X", expectedReceiver: "A"},
    {game: 4, pointsSideA: 8, pointsSideB: 6, expectedServer: "A", expectedReceiver: "X"},
    {game: 4, pointsSideA: 8, pointsSideB: 7, expectedServer: "A", expectedReceiver: "X"},
    {game: 4, pointsSideA: 8, pointsSideB: 8, expectedServer: "X", expectedReceiver: "A"},
    {game: 4, pointsSideA: 8, pointsSideB: 9, expectedServer: "X", expectedReceiver: "A"},
    {game: 4, pointsSideA: 8, pointsSideB: 10, expectedServer: "A", expectedReceiver: "X"},
    {game: 4, pointsSideA: 9, pointsSideB: 10, expectedServer: "A", expectedReceiver: "X"},
    // 2 : 2
    // game 5
    {game: 5, pointsSideA: 0, pointsSideB: 0, expectedServer: "A", expectedReceiver: "X"},
    {game: 5, pointsSideA: 0, pointsSideB: 1, expectedServer: "A", expectedReceiver: "X"},
    {game: 5, pointsSideA: 1, pointsSideB: 1, expectedServer: "X", expectedReceiver: "A"},
    {game: 5, pointsSideA: 1, pointsSideB: 2, expectedServer: "X", expectedReceiver: "A"},
    {game: 5, pointsSideA: 2, pointsSideB: 2, expectedServer: "A", expectedReceiver: "X"},
    {game: 5, pointsSideA: 3, pointsSideB: 2, expectedServer: "A", expectedReceiver: "X"},
    {game: 5, pointsSideA: 4, pointsSideB: 2, expectedServer: "X", expectedReceiver: "A"},
    {game: 5, pointsSideA: 5, pointsSideB: 2, expectedServer: "X", expectedReceiver: "A"},
    {game: 5, pointsSideA: 6, pointsSideB: 2, expectedServer: "A", expectedReceiver: "X"},
    {game: 5, pointsSideA: 7, pointsSideB: 2, expectedServer: "A", expectedReceiver: "X"},
    {game: 5, pointsSideA: 7, pointsSideB: 3, expectedServer: "X", expectedReceiver: "A"},
    {game: 5, pointsSideA: 8, pointsSideB: 3, expectedServer: "X", expectedReceiver: "A"},
    {game: 5, pointsSideA: 8, pointsSideB: 4, expectedServer: "A", expectedReceiver: "X"},
    {game: 5, pointsSideA: 9, pointsSideB: 4, expectedServer: "A", expectedReceiver: "X"},
    {game: 5, pointsSideA: 10, pointsSideB: 4, expectedServer: "X", expectedReceiver: "A"},
    {game: 5, pointsSideA: 10, pointsSideB: 5, expectedServer: "X", expectedReceiver: "A"},
    {game: 5, pointsSideA: 10, pointsSideB: 6, expectedServer: "A", expectedReceiver: "X"},


  ])('Singles game $game at ($pointsSideA, $pointsSideB) -> expected server $expectedServer, expected receiver $expectedReceiver',
    ({game, pointsSideA, pointsSideB, expectedServer, expectedReceiver}) => {
    if (lastSinglesGame != game) {
      lastSinglesGame = game;
        const nextGameIndex = singlesCalculator.startNextGame();
        let nextReceiver = null;
        switch (nextGameIndex) {
          case 1:
            nextReceiver = singlesCalculator.determineNextReceiver('X');
            expect(nextReceiver).toMatch('A');
            singlesCalculator.recordServerAndReceiver('X', nextReceiver);
            break;
          case 2:
            nextReceiver = singlesCalculator.determineNextReceiver('A');
            expect(nextReceiver).toMatch('X');
            singlesCalculator.recordServerAndReceiver('A', nextReceiver);
            break;
          case 3:
            nextReceiver = singlesCalculator.determineNextReceiver('X');
            expect(nextReceiver).toMatch('A');
            singlesCalculator.recordServerAndReceiver('X', nextReceiver);
            break;
          case 4:
            nextReceiver = singlesCalculator.determineNextReceiver('A');
            expect(nextReceiver).toMatch('X');
            singlesCalculator.recordServerAndReceiver('A', nextReceiver);
            break;
        }
      }

      singlesCalculator.determineNextServerAndReceiver(pointsSideA, pointsSideB);
    expect(singlesCalculator.getServer()).toMatch(expectedServer);
    expect(singlesCalculator.getReceiver()).toMatch(expectedReceiver);
  });
});
