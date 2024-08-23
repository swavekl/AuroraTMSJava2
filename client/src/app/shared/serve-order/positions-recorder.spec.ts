import { PositionsRecorder } from './positions-recorder';

describe('PositionsRecorder', () => {
  it('should create an instance', () => {
    const playerAProfiles = 'aaaa1234;aaaa5678';
    const playerBProfiles = 'bbbb1234;bbbb5678';
    let positionsRecorder : PositionsRecorder = new PositionsRecorder(true, playerAProfiles, playerBProfiles);
    expect(positionsRecorder).toBeTruthy();
    let playerPositions = positionsRecorder.getPlayerPositions();
    expect(JSON.stringify(playerPositions)).toMatch("{\"L1\":\"B\",\"L2\":\"A\",\"R1\":\"X\",\"R2\":\"Y\"}");

    positionsRecorder.switchSides();
    playerPositions = positionsRecorder.getPlayerPositions();
    expect(JSON.stringify(playerPositions)).toMatch("{\"L1\":\"X\",\"L2\":\"Y\",\"R1\":\"B\",\"R2\":\"A\"}");

    // right side serving
    positionsRecorder.switchPlayers(false);
    playerPositions = positionsRecorder.getPlayerPositions();
    expect(JSON.stringify(playerPositions)).toMatch("{\"L1\":\"X\",\"L2\":\"Y\",\"R1\":\"A\",\"R2\":\"B\"}");

    // left side receiving
    positionsRecorder.switchPlayers(true);
    playerPositions = positionsRecorder.getPlayerPositions();
    expect(JSON.stringify(playerPositions)).toMatch("{\"L1\":\"Y\",\"L2\":\"X\",\"R1\":\"A\",\"R2\":\"B\"}");

    // serving from left to right
    positionsRecorder.recordPlayerPositions('X', 'A');
    playerPositions = positionsRecorder.getPlayerPositions();
    expect(JSON.stringify(playerPositions)).toMatch("{\"L1\":\"Y\",\"L2\":\"X\",\"R1\":\"A\",\"R2\":\"B\"}");

    // serving from right to left
    positionsRecorder.recordPlayerPositions('A', 'Y');
    playerPositions = positionsRecorder.getPlayerPositions();
    expect(JSON.stringify(playerPositions)).toMatch("{\"L1\":\"X\",\"L2\":\"Y\",\"R1\":\"A\",\"R2\":\"B\"}");

    // serving from left to right again
    positionsRecorder.recordPlayerPositions('Y', 'B');
    playerPositions = positionsRecorder.getPlayerPositions();
    expect(JSON.stringify(playerPositions)).toMatch("{\"L1\":\"X\",\"L2\":\"Y\",\"R1\":\"B\",\"R2\":\"A\"}");

    // serving from right to left again
    positionsRecorder.recordPlayerPositions('B', 'X');
    playerPositions = positionsRecorder.getPlayerPositions();
    expect(JSON.stringify(playerPositions)).toMatch("{\"L1\":\"Y\",\"L2\":\"X\",\"R1\":\"B\",\"R2\":\"A\"}");

  });
});
