package barefoot.sweepervariants;

import barefoot.minesweeper.Constants;

import java.util.Random;

public class SimplestPossibleBot implements SweeperBot {
    /**
     * Implement to set Difficulty for the game
     *
     * @return int[] as found in the difficulty constants in Constants.java
     * @see Constants
     */
    @Override
    public int[] getDifficulty() {
        return Constants.GAME_EASY;
    }

    /**
     * Implement to handle how many times the game should be played.
     * GameStatistics can be fetched from game.getGameStatistics.
     *
     * @param game an instance of the current gui
     * @return boolean that shows if an other round should be played
     */
    @Override
    public boolean playAgain(MyGUISweeper game) {
        //Just one game will be played
        return false;
    }

    /**
     * Implement to calculate the next action
     *
     * @param playerRevealedBoard Double[][] illustrating the gameboard.
     * @param game                an instance of the GUI
     */
    @Override
    public void takeAutomatedAction(Double[][] playerRevealedBoard, MyGUISweeper game) {
        Random random = new Random();
        int row = random.nextInt(10);
        int col = random.nextInt(10);
        game.takeAutomatedAction(Constants.ACTION_SWEEP,row,col);
    }

    public static void main(String[] args) {
        MyGUISweeper.runAutomated(new SimplestPossibleBot());
    }
}
