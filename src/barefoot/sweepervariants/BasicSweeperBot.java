package barefoot.sweepervariants;

import barefoot.minesweeper.Constants;

import java.awt.*;
import java.util.Random;

import static barefoot.minesweeper.Constants.*;

/**
 * Basic bot that can safely sweep and flag all positions
 * that can be calculated with a brute force algorithm.
 * Goal:
 * a, Minimize number of rounds.
 * b, Minimize processing times
 * Things to concider:
 * 1. Better strategies for random sweep
 * a, Optimal start position?
 * b, Calcululation of probabilities for each unswept location
 * c, Estimation of new location with best potential for more information
 * d, ...
 * 2. Identification of patterns
 * a, Identify common patterns
 * b, Find strategies to reduce processing time for identification
 * c, ...
 * 3. Evaluate performance on all three game difficulties.
 */
public class BasicSweeperBot implements SweeperBot {
    private int gameCounter = 0;
    private int antalvinster = 0;
    //Patterns
    private int[][] mönster = {
            {1, 2, 1},
            {1, 3, 2},
            {1, 4},
            {2, 5},
            {3, 6}
    };

    /**
     * Implement to set Difficulty for the game
     *
     * @return int[] as found in the difficulty constants in Constants.java
     * @see Constants
     */
    @Override
    public int[] getDifficulty() {
        return GAME_HARD;
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
        if (game.getGameStatistics().turnsTaken == 1) {
            return true;
        } else {
            System.out.println("GAME DONE");
            //if the game was won
            if (game.getGameStatistics().gameStatus == GAME_WON){
                // Add to antalvinster
                antalvinster++;}
            //calculate the winrate
            double winrate = (double) antalvinster / (gameCounter + 1);
            //Write the winrate
            System.out.printf("Winrate: %.4f\n", winrate);
            System.out.printf("Antal spel: %d\n", gameCounter);
            System.out.println(game.getGameStatistics().toString());
            return ++gameCounter < 500;
        }
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
        //Search for horizontal patterns containing 3 cells
        for (int[] mönster : mönster)
            for (int i = 0; i < getDifficulty()[0]; i++) {
                for (int j = 0; j < getDifficulty()[0]; j++) {
                    if (playerRevealedBoard[i][j] != null && playerRevealedBoard[i][j] == mönster[0] && j + 1 < getDifficulty()[0]) {
                        if (playerRevealedBoard[i][j + 1] != null && playerRevealedBoard[i][j + 1] == mönster[1] && mönster.length == 3) {
                            if (playerRevealedBoard[i][j + 2] != null && playerRevealedBoard[i][j + 2] == mönster[2] && j + 2 < getDifficulty()[0]) {
                                if (takeHorizontalPatternAction(playerRevealedBoard, i, j, game))
                                    return;
                            }
                        }
                    }
                    //Search for vertical patterns containing 3 cells
                    if (playerRevealedBoard[i][j] != null && playerRevealedBoard[i][j] == mönster[0] && i + 1 < getDifficulty()[0]) {
                        if (playerRevealedBoard[i + 1][j] != null && playerRevealedBoard[i + 1][j] == mönster[1] && mönster.length == 3) {
                            if (playerRevealedBoard[i + 2][j] != null && playerRevealedBoard[i + 2][j] == mönster[2] && i + 2 < getDifficulty()[0]) {
                                if (takeVerticalPatternAction(playerRevealedBoard, i, j, game))
                                    return;
                            }
                        }
                    }
                    //Search for horizontal patterns containing 2 cells
                    if (playerRevealedBoard[i][j] != null && playerRevealedBoard[i][j] == mönster[0] && j + 1 < getDifficulty()[0]) {
                        if (playerRevealedBoard[i][j + 1] != null && playerRevealedBoard[i][j + 1] == mönster[1] && mönster.length == 2) {
                            if (takeHorizontalPatternAction(playerRevealedBoard, i, j, game))
                                return;
                        }
                    }
                    //Search for vertical patterns containing 2 cells
                    if (playerRevealedBoard[i][j] != null && playerRevealedBoard[i][j] == mönster[0] && i + 1 < getDifficulty()[0]) {
                        if (playerRevealedBoard[i + 1][j] != null && playerRevealedBoard[i + 1][j] == mönster[1] && mönster.length == 2) {
                            if (takeVerticalPatternAction(playerRevealedBoard, i, j, game))
                                return;
                        }
                    }
                }
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
     * If a vertical patterns was found, the method checks if the cells that are supposed to be either swept of flagged are grass,
     * if that is the case an action will be performed on that specific cell
     * @param playerRevealedBoard Double[][] illustrating the gameboard.
     *                            null --> not swept cell
     * @param i position on the vertical line
     * @param j position on the horizontal line
     * @param game an instance of the gui
     * @return boolean, if true an action has been performed on a cell
     *                  if false no action has been performed
     */
    private boolean takeVerticalPatternAction(Double[][] playerRevealedBoard, int i, int j, MyGUISweeper game) {
        //1,3,2
        if (playerRevealedBoard[i][j] == 1 && playerRevealedBoard[i + 1][j] == 3 && playerRevealedBoard[i + 2][j] == 2) {
            if (j - 1 >= 0 && i + 2 < getDifficulty()[0]) {
                if (playerRevealedBoard[i + 2][j - 1] == null) {
                    game.takeAutomatedAction(ACTION_FLAG, i + 2, j - 1);
                    return true;
                }
            }
            if (j + 1 < getDifficulty()[0] && i + 2 < getDifficulty()[0]) {
                if (playerRevealedBoard[i + 2][j + 1] == null) {
                    game.takeAutomatedAction(ACTION_FLAG, i + 2, j + 1);
                    return true;
                }
            }
            return VerticalEightSweep(playerRevealedBoard, i, j, game);

        }
        //1,2,1
        if (playerRevealedBoard[i][j] == 1 && playerRevealedBoard[i + 1][j] == 2 && playerRevealedBoard[i + 2][j] == 1) {
            return VerticalEightSweep(playerRevealedBoard, i, j, game);
        }
        //1,4 2,5 3,6
        if ((playerRevealedBoard[i][j] == 1 && playerRevealedBoard[i + 1][j] == 4) ||
                (playerRevealedBoard[i][j] == 2 && playerRevealedBoard[i + 1][j] == 5) ||
                (playerRevealedBoard[i][j] == 3 && playerRevealedBoard[i + 1][j] == 6) && mönster.length == 2) {
            if (i + 2 < getDifficulty()[0]) {
                if (i - 1 >= 0 && playerRevealedBoard[i + 2][j - 1] == null) {
                    game.takeAutomatedAction(ACTION_FLAG, i + 2, j - 1);
                    return true;
                }
                if (playerRevealedBoard[i + 2][j] == null) {
                    game.takeAutomatedAction(ACTION_FLAG, i + 2, j);
                    return true;
                }
                if (j + 1 < getDifficulty()[0] && playerRevealedBoard[i + 2][j + 1] == null) {
                    game.takeAutomatedAction(ACTION_FLAG, i + 2, j + 1);
                    return true;
                }
            }
            if (i - 1 >= 0) {
                if (j - 1 >= 0 && playerRevealedBoard[i - 1][j - 1] == null) {
                    game.takeAutomatedAction(ACTION_SWEEP, i - 1, j - 1);
                    return true;
                }
                if (playerRevealedBoard[i - 1][j] == null) {
                    game.takeAutomatedAction(ACTION_SWEEP, i - 1, j);
                    return true;
                }
                if (j + 1 < getDifficulty()[0] && playerRevealedBoard[i - 1][j + 1] == null) {
                    game.takeAutomatedAction(ACTION_SWEEP, i - 1, j + 1);
                    return true;
                }
            }

        }
        return false;
    }

    /**
     * the vertical patterns (1,3,2) and (1,2,1) sweeps the same cells. Therefore the cells that are supposed to be swept
     * are checked if they are null, if not the cells will be swept.
     * @param playerRevealedBoard Double[][] illustrating the gameboard.
     *                            null --> not swept cell
     * @param i position on the vertical line
     * @param j position on the horizontal line
     * @param game an instance of the gui
     * @return boolean, if true an action has been performed on a cell
     *                  if false no action has been performed
     */
    private boolean VerticalEightSweep(Double[][] playerRevealedBoard, int i, int j, MyGUISweeper game) {
        if (j - 1 >= 0 && i + 1 < getDifficulty()[0]) {
            if (playerRevealedBoard[i + 1][j - 1] == null) {
                game.takeAutomatedAction(ACTION_SWEEP, i + 1, j - 1);
                return true;
            }
        }
        if (i + 1 < getDifficulty()[0] && j + 1 < getDifficulty()[0]) {
            if (playerRevealedBoard[i + 1][j + 1] == null) {
                game.takeAutomatedAction(ACTION_SWEEP, i + 1, j + 1);
                return true;
            }
        }
        if (i - 1 >= 0) {
            if (j + 1 < getDifficulty()[0] && playerRevealedBoard[i - 1][j + 1] == null) {
                game.takeAutomatedAction(ACTION_SWEEP, i - 1, j + 1);
                return true;
            }
            if (playerRevealedBoard[i - 1][j] == null) {
                game.takeAutomatedAction(ACTION_SWEEP, i - 1, j);
                return true;
            }
            if (j - 1 >= 0 && playerRevealedBoard[i - 1][j - 1] == null) {
                game.takeAutomatedAction(ACTION_SWEEP, i - 1, j - 1);
                return true;
            }

        }
        if (i + 3 < getDifficulty()[0]) {
            if (j + 1 < getDifficulty()[0] && playerRevealedBoard[i + 3][j + 1] == null) {
                game.takeAutomatedAction(ACTION_SWEEP, i + 3, j + 1);
                return true;
            }
            if (playerRevealedBoard[i + 3][j] == null) {
                game.takeAutomatedAction(ACTION_SWEEP, i + 3, j);
                return true;
            }
            if (j - 1 >= 0 && playerRevealedBoard[i + 3][j - 1] == null) {
                game.takeAutomatedAction(ACTION_SWEEP, i + 3, j - 1);
                return true;
            }
        }
        return false;
    }
    /**
     * If a horizontal patterns was found, the method checks if the cells that are supposed to be either swept of flagged are grass,
     * if that is the case an action will be performed on that specific cell
     * @param playerRevealedBoard Double[][] illustrating the gameboard.
     *                            null --> not swept cell
     * @param i position on the vertical line
     * @param j position on the horizontal line
     * @param game an instance of the gui
     * @return boolean, if true an action has been performed on a cell
     *                  if false no action has been performed
     */
    private boolean takeHorizontalPatternAction(Double[][] playerRevealedBoard, int i, int j, MyGUISweeper game) {
        //1,3,2
        if (playerRevealedBoard[i][j] == 1 && playerRevealedBoard[i][j + 1] == 3 && playerRevealedBoard[i][j + 2] == 2) {
            if (i - 1 >= 0 && j + 2 < getDifficulty()[0]) {
                if (playerRevealedBoard[i - 1][j + 2] == null) {
                    game.takeAutomatedAction(ACTION_FLAG, i - 1, j + 2);
                    return true;
                }
            }
            if (i + 1 < getDifficulty()[0] && j + 2 < getDifficulty()[0]) {
                if (playerRevealedBoard[i + 1][j + 2] == null) {
                    game.takeAutomatedAction(ACTION_FLAG, i + 1, j + 2);
                    return true;
                }
            }
            return HorizontalEightSweep(playerRevealedBoard, i, j, game);
        }
        //1,2,1
        if (playerRevealedBoard[i][j] == 1 && playerRevealedBoard[i][j + 1] == 2 && playerRevealedBoard[i][j + 2] == 1) {
            return HorizontalEightSweep(playerRevealedBoard, i, j, game);
        }
        // 1,4 & 2,5 & 3,6
        if ((playerRevealedBoard[i][j] == 1 && playerRevealedBoard[i][j + 1] == 4) ||
                (playerRevealedBoard[i][j] == 2 && playerRevealedBoard[i][j + 1] == 5) ||
                (playerRevealedBoard[i][j] == 3 && playerRevealedBoard[i][j + 1] == 6) && mönster.length == 2) {
            if (j + 2 < getDifficulty()[0]) {
                if (i - 1 >= 0 && playerRevealedBoard[i - 1][j + 2] == null) {
                    game.takeAutomatedAction(ACTION_FLAG, i - 1, j + 2);
                    return true;
                }
                if (playerRevealedBoard[i][j + 2] == null) {
                    game.takeAutomatedAction(ACTION_FLAG, i, j + 2);
                    return true;
                }
                if (i + 1 < getDifficulty()[0] && playerRevealedBoard[i + 1][j + 2] == null) {
                    game.takeAutomatedAction(ACTION_FLAG, i + 1, j + 2);
                    return true;
                }
            }
            if (j - 1 >= 0) {
                if (i - 1 >= 0 && playerRevealedBoard[i - 1][j - 1] == null) {
                    game.takeAutomatedAction(ACTION_SWEEP, i - 1, j - 1);
                    return true;
                }
                if (playerRevealedBoard[i][j - 1] == null) {
                    game.takeAutomatedAction(ACTION_SWEEP, i, j - 1);
                    return true;
                }
                if (i + 1 < getDifficulty()[0] && playerRevealedBoard[i + 1][j - 1] == null) {
                    game.takeAutomatedAction(ACTION_SWEEP, i + 1, j - 1);
                    return true;
                }
            }

        }
        return false;
    }
    /**
     * the vertical patterns (1,3,2) and (1,2,1) sweeps the same cells. Therefore the cells that are supposed to be swept
     * are checked if they are null, if not the cells will be swept.
     * @param playerRevealedBoard Double[][] illustrating the gameboard.
     *                            null --> not swept cell
     * @param i position on the vertical line
     * @param j position on the horizontal line
     * @param game an instance of the gui
     * @return boolean, if true an action has been performed on a cell
     *                  if false no action has been performed
     */
    private boolean HorizontalEightSweep(Double[][] playerRevealedBoard, int i, int j, MyGUISweeper game) {
        if (i - 1 >= 0 && j + 1 < getDifficulty()[0]) {
            if (playerRevealedBoard[i - 1][j + 1] == null) {
                game.takeAutomatedAction(ACTION_SWEEP, i - 1, j + 1);
                return true;
            }
        }
        if (j + 1 < getDifficulty()[0] && i + 1 < getDifficulty()[0]) {
            if (playerRevealedBoard[i + 1][j + 1] == null) {
                game.takeAutomatedAction(ACTION_SWEEP, i + 1, j + 1);
                return true;
            }
        }
        if (j - 1 >= 0) {
            if (i + 1 < getDifficulty()[0] && playerRevealedBoard[i + 1][j - 1] == null) {
                game.takeAutomatedAction(ACTION_SWEEP, i + 1, j - 1);
                return true;
            }
            if (playerRevealedBoard[i][j - 1] == null) {
                game.takeAutomatedAction(ACTION_SWEEP, i, j - 1);
                return true;
            }
            if (i - 1 >= 0 && playerRevealedBoard[i - 1][j - 1] == null) {
                game.takeAutomatedAction(ACTION_SWEEP, i - 1, j - 1);
                return true;
            }

        }
        if (j + 3 < getDifficulty()[0]) {
            if (i + 1 < getDifficulty()[0] && playerRevealedBoard[i + 1][j + 3] == null) {
                game.takeAutomatedAction(ACTION_SWEEP, i + 1, j + 3);
                return true;
            }
            if (playerRevealedBoard[i][j + 3] == null) {
                game.takeAutomatedAction(ACTION_SWEEP, i, j + 3);
                return true;
            }
            if (i - 1 >= 0 && playerRevealedBoard[i - 1][j + 3] == null) {
                game.takeAutomatedAction(ACTION_SWEEP, i - 1, j + 3);
                return true;
            }
        }
        return false;
    }

    /**
     * If the supplied location is open and has a number, the bot will try to
     * find a suitable location to sweep. If no such location can be found,
     * the bot will try to find a suitable location to flag. If no such location
     * can be found, the bot will abstain from action.
     *
     * @param game                an instance of the GUI
     * @param playerRevealedBoard Double[][] illustrating the gameboard.
     *                            null -> not sweeped yet
     *                            0.0 -> visual bomb
     *                            Double.MAX -> Flagged position
     *                            1-8 -> Number of adjacent bombs
     * @param location            Point to analyze
     * @return boolean telling the caller if an action could be performed
     */
    private boolean takeSafeBasicActionIfPossible(MyGUISweeper game, Double[][] playerRevealedBoard, Point location) {
        Double cellValue = playerRevealedBoard[location.x][location.y];
        if (cellValue == null || cellValue < 1 || cellValue > 8)
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
     *
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
     *
     * @param playerRevealedBoard Double[][] illustrating the gameboard.
     *                            *                            null -> not sweeped yet
     *                            *                            0.0 -> visual bomb
     *                            *                            Double.MAX -> Flagged position
     *                            *                            1-8 -> Number of adjacent bombs
     * @param location            Point to be analyzed
     * @param flagedBombsOnly     boolean to indicate if sweepable locations (TRUE)
     *                            or locations to flag should be searched for.
     * @param nextLocation        Point with a safe position to take action on. If x-value is negativ,
     *                            no safe location could be found.
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
                    } else
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

