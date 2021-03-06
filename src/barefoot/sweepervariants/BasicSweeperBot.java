package barefoot.sweepervariants;

import barefoot.minesweeper.Constants;
import barefoot.sweepervariants.gui.MyGUISweeper;

import java.awt.*;
import java.util.Random;

import static barefoot.minesweeper.Constants.*;

/**
 * Basic bot that can safely sweep and flag all positions
 * that can be calculated with a brute force algorithm.
 * Goal:
 *      a, Minimize number of rounds.
 *      b, Minimize processing times
 * Things to concider:
 * 1. Better strategies for random sweep
 *      a, Optimal start position?
 *      b, Calcululation of probabilities for each unswept location
 *      c, Estimation of new location with best potential for more information
 *      d, ...
 * 2. Identification of patterns
 *      a, Identify common patterns
 *      b, Find strategies to reduce processing time for identification
 *      c, ...
 * 3. Evaluate performance on all three game difficulties.
 */
public class BasicSweeperBot implements SweeperBot {
    private int gameCounter = 0;

    /**
     * Implement to set Difficulty for the game
     *
     * @return int[] as found in the difficulty constants in Constants.java
     * @see Constants
     */
    @Override
    public int[] getDifficulty() {
        return GAME_EASY;
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
        System.out.println("GAME DONE");
        System.out.println(game.getGameStatistics().toString());
        return ++gameCounter < maxRounds;
    }

    /**
     * Implement to calculate the next action
     * Do not make multiple calls to game.takeAutomatedAction(), only one.
     *
     * @param playerRevealedBoard Double[][] illustrating the gameboard.
     *                            null -> not sweeped yet
     *                            0.0 -> visual bomb
     *                            Double.MAX -> Flagged position
     *                            1-8 -> Number of adjacent bombs
     * @param game                an instance of the GUI
     */
    @Override
    public void takeAutomatedAction(Double[][] playerRevealedBoard, MyGUISweeper game) {
        //Hold processing to show previous move on screen
        try {
            Thread.sleep(waitForNextMove);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Point startLocation = new Point(0, 0);
        do {
            if (takeSafeBasicActionIfPossible(game, playerRevealedBoard, startLocation))
                return;
            startLocation = getNextLocation(startLocation);
        } while (startLocation != null);

        //This is the last resort when we are desperate for progression
        System.out.println("TIME FOR RANDOM SWEEP!!!");
        int row;
        int col;
        do {
            Random random = new Random();
            row = random.nextInt(playerRevealedBoard.length);
            col = random.nextInt(playerRevealedBoard[row].length);
        } while (playerRevealedBoard[row][col] != null);
        game.takeAutomatedAction(ACTION_SWEEP, row, col);
    }

    /**
     * If the supplied location is open and has a number, the bot will try to
     * find a suitable location to sweep. If no such location can be found,
     * the bot will try to find a suitable location to flag. If no such location
     * can be found, the bot will abstain from action.
     * @param game                an instance of the GUI
     * @param playerRevealedBoard Double[][] illustrating the gameboard.
     *                            null -> not sweeped yet
     *                            0.0 -> visual bomb
     *                            Double.MAX -> Flagged position
     *                            1-8 -> Number of adjacent bombs
     * @param location Point to analyze
     * @return boolean telling the caller if an action could be performed
     */
    private boolean takeSafeBasicActionIfPossible(MyGUISweeper game, Double[][] playerRevealedBoard, Point location) {
        Double cellValue = playerRevealedBoard[location.x][location.y];
        if(cellValue == null || cellValue < 1 || cellValue > 8)
            return false;
        Point nextActionLocation = new Point(-1, 0);

        findNextSafeBasicActionLocation(playerRevealedBoard, location, true, nextActionLocation);
        if (nextActionLocation.x >= 0) {
            game.takeAutomatedAction(ACTION_SWEEP, nextActionLocation.x, nextActionLocation.y);
            return true;
        }

        findNextSafeBasicActionLocation(playerRevealedBoard, location, false, nextActionLocation);
        if (nextActionLocation.x >= 0) {
            game.takeAutomatedAction(ACTION_FLAG, nextActionLocation.x, nextActionLocation.y);
            return true;
        }
        return false;
    }

    /**
     * Gets the next location in the matrix, following the supplied
     * startLocation
     * @param startLocation Point in the matrix to use for calculation
     *                      of next location.
     * @return Point in the matrix for the location following the startLocation
     */
    private Point getNextLocation(Point startLocation) {
        if (startLocation.y < getDifficulty()[1] - 1)
            return new Point(startLocation.x, startLocation.y + 1);
        if (startLocation.x < getDifficulty()[0] - 1)
            return new Point(startLocation.x + 1, 0);
        return null;
    }

    /**
     * An attempt is made to find the next location to flag or sweep. A sweepable location can be found
     * if "flagedBombsOnly" is TRUE, else a position to flag can be found.
     * If no safe location can be found, nextLocation will have a negative value for its x-position.
     * @param playerRevealedBoard Double[][] illustrating the gameboard.
     *      *                            null -> not sweeped yet
     *      *                            0.0 -> visual bomb
     *      *                            Double.MAX -> Flagged position
     *      *                            1-8 -> Number of adjacent bombs
     * @param location Point to be analyzed
     * @param flagedBombsOnly boolean to indicate if sweepable locations (TRUE)
     *                        or locations to flag should be searched for.
     * @param nextLocation Point with a safe position to take action on. If x-value is negativ,
     *                     no safe location could be found.
     */
    private void findNextSafeBasicActionLocation(Double[][] playerRevealedBoard, Point location, boolean flagedBombsOnly, Point nextLocation) {
        int potentialBombs = 0;
        Point firstFoundActionableLocation = null;
        for (int k = location.x - 1; k <= location.x + 1; k++) {
            for (int l = location.y - 1; l <= location.y + 1; l++) {
                //don't inspect outside the matrix
                if (k < 0 || l < 0 || k >= getDifficulty()[0] || l >= getDifficulty()[1])
                    continue;
                if (playerRevealedBoard[k][l] == null || playerRevealedBoard[k][l] == Double.MAX_VALUE) {
                    if (playerRevealedBoard[k][l] == null) {
                        firstFoundActionableLocation = new Point(k, l);
                        potentialBombs = flagedBombsOnly ? potentialBombs : potentialBombs + 1;
                    }
                    else
                        potentialBombs++;
                }
            }
        }
        if (potentialBombs == playerRevealedBoard[location.x][location.y] && firstFoundActionableLocation != null) {
            nextLocation.x = firstFoundActionableLocation.x;
            nextLocation.y = firstFoundActionableLocation.y;
            return;
        }
        nextLocation.x = -1;
    }

    public static void main(String[] args) {
        MyGUISweeper.runAutomated(new BasicSweeperBot());
    }
}

