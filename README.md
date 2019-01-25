# MineSweeper_Automated
A simple implementaion of the classic game MineSweeper. Intended to work as a starting point to experiment with different algorithms for solving the game.

Graphics resources used in the examples can be downloaded at icon8, if used in your implementation, be sure to add credits to the designers.

#How to use
In the project you will find an example of how to build a RandomSweeperBot. To create your own SweepreBot, just implement the interface SweeperBot and its methods. Simplest possible bot would look like this:

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
