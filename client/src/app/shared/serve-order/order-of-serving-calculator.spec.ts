import {OrderOfServingCalculator} from './order-of-serving-calculator';

describe('OrderOfServingCalculator', () => {
  // doubles
  let calculator = new OrderOfServingCalculator(5, true, '','');
  // A, B on the right
  calculator.switchSides();

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
        calculator.switchSides();
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

  // doubles
  let doublesCalculator2 = new OrderOfServingCalculator(5, true, '','');
  // A, B on the right
  doublesCalculator2.switchSides();

  doublesCalculator2.recordServerAndReceiver('A', 'X');
  let lastDoublesGame2 = 1;
  it('Doubles initial server A', () => {
    expect(doublesCalculator2.getServer()).toMatch('A');
  });
  it('Doubles initial receiver X', () => {
    expect(doublesCalculator2.getReceiver()).toMatch('X');
  });

  test.each([
    {game: 1, pointsSideA: 0, pointsSideB: 0, expectedServer: "A", expectedReceiver: "X"},
    {game: 1, pointsSideA: 0, pointsSideB: 1, expectedServer: "A", expectedReceiver: "X"},
    {game: 1, pointsSideA: 1, pointsSideB: 1, expectedServer: "X", expectedReceiver: "B"},
    {game: 1, pointsSideA: 2, pointsSideB: 1, expectedServer: "X", expectedReceiver: "B"},
    // reverse score 1:2  -> 2:1
    {game: 1, pointsSideA: 1, pointsSideB: 1, expectedServer: "X", expectedReceiver: "B"},
    {game: 1, pointsSideA: 1, pointsSideB: 2, expectedServer: "X", expectedReceiver: "B"},

    {game: 1, pointsSideA: 3, pointsSideB: 1, expectedServer: "B", expectedReceiver: "Y"},
    {game: 1, pointsSideA: 3, pointsSideB: 2, expectedServer: "B", expectedReceiver: "Y"},
    {game: 1, pointsSideA: 3, pointsSideB: 3, expectedServer: "Y", expectedReceiver: "A"},
    // // reverse again 3:3 -> 4:2
    {game: 1, pointsSideA: 2, pointsSideB: 3, expectedServer: "B", expectedReceiver: "Y"},
    {game: 1, pointsSideA: 2, pointsSideB: 4, expectedServer: "Y", expectedReceiver: "A"},

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


  ])('Reverse score - Doubles game $game at ($pointsSideA, $pointsSideB) -> expected server $expectedServer, expected receiver $expectedReceiver',
    ({game, pointsSideA, pointsSideB, expectedServer, expectedReceiver}) => {
      if (lastDoublesGame2 != game) {
        lastDoublesGame2 = game;
        const nextGameIndex = doublesCalculator2.startNextGame();
        doublesCalculator2.switchSides();
        let nextReceiver = null;
        switch (nextGameIndex) {
          case 1:
            nextReceiver = doublesCalculator2.determineNextReceiver('Y');
            expect(nextReceiver).toMatch('B');
            doublesCalculator2.recordServerAndReceiver('Y', nextReceiver);
            break;
          case 2:
            nextReceiver = doublesCalculator2.determineNextReceiver('B');
            expect(nextReceiver).toMatch('Y');
            doublesCalculator2.recordServerAndReceiver('B', nextReceiver);
            break;
          case 3:
            nextReceiver = doublesCalculator2.determineNextReceiver('X');
            expect(nextReceiver).toMatch('A');
            doublesCalculator2.recordServerAndReceiver('X', nextReceiver);
            break;
          case 4:
            nextReceiver = doublesCalculator2.determineNextReceiver('B');
            expect(nextReceiver).toMatch('Y');
            doublesCalculator2.recordServerAndReceiver('B', nextReceiver);
            break;
        }
      }

      doublesCalculator2.determineNextServerAndReceiver(pointsSideA, pointsSideB);
    expect(doublesCalculator2.getServer()).toMatch(expectedServer);
    expect(doublesCalculator2.getReceiver()).toMatch(expectedReceiver);
  });


  let singlesCalculator = new OrderOfServingCalculator(5, false, '', '');
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
        singlesCalculator.switchSides();
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

  /**
   * test changing score after mistake
   */

  let singlesCalculator2 = new OrderOfServingCalculator(5, false, '', '');
  singlesCalculator2.recordServerAndReceiver('A', 'X');
  let lastSinglesGame2 = 1;
  it('Singles initial server A', () => {
    expect(singlesCalculator2.getServer()).toMatch('A');
  });
  it('Singles initial receiver X', () => {
    expect(singlesCalculator2.getReceiver()).toMatch('X');
  });

  test.each([
    {game: 1, pointsSideA: 0, pointsSideB: 0, expectedServer: "A", expectedReceiver: "X"},
    {game: 1, pointsSideA: 0, pointsSideB: 1, expectedServer: "A", expectedReceiver: "X"},
    {game: 1, pointsSideA: 1, pointsSideB: 1, expectedServer: "X", expectedReceiver: "A"},
    {game: 1, pointsSideA: 2, pointsSideB: 1, expectedServer: "X", expectedReceiver: "A"},
    // fix score instead 2:1 to 1:1 and then w:2
    {game: 1, pointsSideA: 1, pointsSideB: 1, expectedServer: "X", expectedReceiver: "A"},
    {game: 1, pointsSideA: 1, pointsSideB: 2, expectedServer: "X", expectedReceiver: "A"},

    {game: 1, pointsSideA: 3, pointsSideB: 1, expectedServer: "A", expectedReceiver: "X"},
    {game: 1, pointsSideA: 3, pointsSideB: 2, expectedServer: "A", expectedReceiver: "X"},
    {game: 1, pointsSideA: 3, pointsSideB: 3, expectedServer: "X", expectedReceiver: "A"},
    // reverse again from 3:3 -> 2:4
    {game: 1, pointsSideA: 2, pointsSideB: 3, expectedServer: "A", expectedReceiver: "X"},
    {game: 1, pointsSideA: 2, pointsSideB: 4, expectedServer: "X", expectedReceiver: "A"},

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
    // fix score to 10:11
    {game: 2, pointsSideA: 10, pointsSideB: 10, expectedServer: "X", expectedReceiver: "A"},
    {game: 2, pointsSideA: 10, pointsSideB: 11, expectedServer: "A", expectedReceiver: "X"},

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


  ])('Reverse score - Singles game $game at ($pointsSideA, $pointsSideB) -> expected server $expectedServer, expected receiver $expectedReceiver',
    ({game, pointsSideA, pointsSideB, expectedServer, expectedReceiver}) => {
    if (lastSinglesGame2 != game) {
      lastSinglesGame2 = game;
        const nextGameIndex = singlesCalculator2.startNextGame();
        singlesCalculator2.switchSides();
        let nextReceiver = null;
        switch (nextGameIndex) {
          case 1:
            nextReceiver = singlesCalculator2.determineNextReceiver('X');
            expect(nextReceiver).toMatch('A');
            singlesCalculator2.recordServerAndReceiver('X', nextReceiver);
            break;
          case 2:
            nextReceiver = singlesCalculator2.determineNextReceiver('A');
            expect(nextReceiver).toMatch('X');
            singlesCalculator2.recordServerAndReceiver('A', nextReceiver);
            break;
          case 3:
            nextReceiver = singlesCalculator2.determineNextReceiver('X');
            expect(nextReceiver).toMatch('A');
            singlesCalculator2.recordServerAndReceiver('X', nextReceiver);
            break;
          case 4:
            nextReceiver = singlesCalculator2.determineNextReceiver('A');
            expect(nextReceiver).toMatch('X');
            singlesCalculator2.recordServerAndReceiver('A', nextReceiver);
            break;
        }
      }

      singlesCalculator2.determineNextServerAndReceiver(pointsSideA, pointsSideB);
    expect(singlesCalculator2.getServer()).toMatch(expectedServer);
    expect(singlesCalculator2.getReceiver()).toMatch(expectedReceiver);
  });
});
