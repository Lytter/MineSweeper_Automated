package barefoot.sweepervariants;

import barefoot.sweepervariants.gui.MyGUISweeper;

public interface SweeperBot {
    /**
     * Implement to set Difficulty for the game
     * @return int[] as found in the difficulty constants in Constants.java
     * @see barefoot.minesweeper.Constants
     */
    int[] getDifficulty();

    /**
     * Implement to handle how many times the game should be played.
     * GameStatistics can be fetched from game.getGameStatistics.
     * @param game an instance of the current gui
     * @return boolean that shows if an other round should be played
     */
    boolean playAgain(MyGUISweeper game);

    /**
     * Implement to calculate the next action
     * Do not make multiple calls to game.takeAutomatedAction(), only one.
     * @param playerRevealedBoard Double[][] illustrating the gameboard.
     *                            null -> not sweeped yet
     *                            0.0 -> visual bomb
     *                            Double.MAX -> Flagged position
     *                            1-8 -> Number of adjacent bombs
     * @param game an instance of the GUI
     */
    void takeAutomatedAction(Double[][] playerRevealedBoard, MyGUISweeper game);
}
