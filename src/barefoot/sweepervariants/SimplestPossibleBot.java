package barefoot.sweepervariants;

import barefoot.minesweeper.Constants;
import barefoot.sweepervariants.gui.MyGUISweeper;

import java.util.Random;

/**
 * This bot will play one game on EASY difficulty.
 * All sweeps are random. Utterly useless at paying the game, but
 * might be a good starting point for a first project.
 */
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
     * Do not make multiple calls to game.takeAutomatedAction(), only one.
     *
     * @param playerRevealedBoard Double[][] illustrating the gameboard.
     * @param game                an instance of the GUI
     */
    @Override
    public void takeAutomatedAction(Double[][] playerRevealedBoard, MyGUISweeper game) {
        Random random = new Random();
        int row = random.nextInt(Constants.GAME_EASY[1]);
        int col = random.nextInt(Constants.GAME_EASY[1]);
        if (random.nextBoolean())
            game.takeAutomatedAction(Constants.ACTION_SWEEP,row,col);
        else
            game.takeAutomatedAction(Constants.ACTION_FLAG, row, col);
        System.out.println(game.toString(Constants.PLAYER_MATRIX));
    }

    public static void main(String[] args) {
        MyGUISweeper.runAutomated(new SimplestPossibleBot());
    }
}
