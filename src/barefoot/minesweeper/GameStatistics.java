package barefoot.minesweeper;

/**
 * Class holds the statistics for a game of minesweeper
 */
public class GameStatistics {
    public int gameStatus = Constants.GAME_INPROGRESS;
    public int turnsTaken = 0;
    //Time in milliseconds
    private final long gameStarted;
    private long gameEnded = 0;

    public GameStatistics() {
        gameStarted = System.currentTimeMillis();
    }
    public long getPlayedTime() {
        if (gameEnded > 0)
            return gameEnded - gameStarted;
        else
            return System.currentTimeMillis() - gameStarted;
    }
    public void stopGame() {
        if (gameEnded > 0)
            return;
        gameEnded = System.currentTimeMillis();
    }
    public String toString() {
        return "GameStatus: " + gameStatus +
                "\nRounds played: " + turnsTaken +
                "\nTime elapsed: " + getPlayedTime();
    }
}
