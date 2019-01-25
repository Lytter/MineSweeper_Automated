package barefoot.minesweeper;

import java.util.Arrays;
import java.util.Random;
import java.util.Stack;

import static barefoot.minesweeper.Constants.*;

/**
 * This class encapsules functionality for a game of
 * MineSweeper. This class is intended to be used
 * to test different algorithms for computing the best location to
 * sweep next.
 * @author Andreas Lytter, andreas.lytter@rosendalsgymnasiet.se
 * @version 1.0
 */
public class Engine {
    //game matrix, true = bomb, false = no bomb
    private boolean[][] gameMatrix;
    //the probability matrix, 0 = bomb, -1 = no bomb in sight, 1-8 = adjecent bombs
    private int[][] probabilityMatrix;
    //player matrix, null = not revieled yet, else same as probability matrix
    private Double[][] playerRevealedMatrix;

    //Number of rows in matrix
    private int rows;
    //Number of cols in matrix
    private int cols;
    //Data structure to handle deep recursion
    private Stack<int[]> latestPositions;
    //Data about the current game
    private GameStatistics gameStats;

    /**
     * Constructs an engine for playing a game of MineSweeper
     * @param matrixRows int determines the number of rows of the matrix
     * @param matrixCols int determines the number of columns of the matrix
     * @param bombs int determines the number of bombs that are placed in the matrix
     */
    public Engine(int matrixRows, int matrixCols, int bombs) {
        rows = matrixRows; cols = matrixCols;
        latestPositions = new Stack<>();
        gameMatrix = generateGameMatrix(matrixRows, matrixCols, bombs);
        probabilityMatrix = generateProbabilityMatrix(matrixRows, matrixCols);
        gameStats = new GameStatistics();
    }

    /**
     * Generates a probability base for the mainfild in game_matrix.
     * -1 = no adjecent bombs, 0 = a bomb, 1-8 = number of adjecent bombs
     * @param matrixRows int determines the number of rows of the matrix
     * @param matrixCols int determines the number of columns of the matrix
     * @return int[][] the matrix
     */
    private int[][] generateProbabilityMatrix(int matrixRows, int matrixCols) {
        int[][] matrix = new int[matrixRows][matrixCols];
        for (int i = 0; i < matrixRows; i++) {
            for (int j = 0; j < matrixCols; j++) {
                int bombsFound = 0;
                if (!gameMatrix[i][j]) {
                    for (int k = i-1; k <= i+1; k++) {
                        for (int l = j-1; l <= j+1; l++) {
                            if(k >= 0 && l >= 0)
                                if (k < rows && l < cols)
                                    bombsFound += gameMatrix[k][l] ? 1 : 0;
                        }
                    }
                    bombsFound = bombsFound == 0 ? -1 : bombsFound;
                }
                matrix[i][j] = bombsFound;
            }
        }
        return matrix;
    }

    /**
     * Generate a matrix that represent the minefield.
     * true = bomb, false = no bomb
     * @param matrixRows int determines the number of rows of the matrix
     * @param matrixCols int determines the number of columns of the matrix
     * @param bombs int number of bombs to be placed in the field
     * @return boolean[][] the matrix
     */
    private boolean[][] generateGameMatrix(int matrixRows, int matrixCols, int bombs) {
        boolean[][] matrix = new boolean[matrixRows][matrixCols];
        for (int i = 0; i < bombs; i++) {
            int col, row;
            do {
                col = new Random().nextInt(matrixRows);
                row = new Random().nextInt(matrixCols);
            }while(matrix[col][row]);
            matrix[col][row] = true;
        }
        return matrix;
    }

    /**
     * Reveals a location to the user. If that location has no adjecent bombs,
     * all surrounding locations are also revealed.
     * @param x int column to reveal
     * @param y int row to reveal
     */
    public void sweepLocation(int x, int y) {
        sweepLocation(x, y, 0);
    }

    /**
     * Reveals a location to the user. If that location has no adjecent bombs,
     * all surrounding locations are also revealed.
     * @param x int column to reveal
     * @param y int row to reveal
     * @param depth int internal recursion counter
     */
    private void sweepLocation(int x, int y, int depth) {
        if (playerRevealedMatrix == null)
            playerRevealedMatrix = new Double[rows][cols];
        if (x < 0 || x > rows-1 || y < 0 || y > cols-1)
            return; //utanför grid
        if (playerRevealedMatrix[x][y] != null && playerRevealedMatrix[x][y] > Double.NEGATIVE_INFINITY)
            return; //redan hanterad
        if (depth == 300) {
            playerRevealedMatrix[x][y] = Double.NEGATIVE_INFINITY;
            latestPositions.push(new int[]{x,y});
            return;
        }
        playerRevealedMatrix[x][y] = (double)probabilityMatrix[x][y];
        if (playerRevealedMatrix[x][y] == -1) {
            for (int i = x-1; i <= x+1; i++) {
                for (int j = y-1; j <= y+1; j++) {
                    sweepLocation(i, j, depth +1);
                }
            }
        }
        if(depth == 0) {
            gameStats.turnsTaken++;
            int[] latestPos;
            latestPos = latestPositions.size() != 0 ? latestPositions.pop() : new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE};
            while (latestPos[0] > Integer.MIN_VALUE && latestPos[1] > Integer.MIN_VALUE) {
                sweepLocation(latestPos[0], latestPos[1], depth+1);
                latestPos = latestPositions.size() != 0 ? latestPositions.pop() : new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE};
            }
        }
    }

    /**
     * Returns a deep copy of the game board matrix.
     * @return Double[][] matrix representing the game board
     */
    public Double[][] getPlayerRevealedMatrix() {
        return copyOfMatrix(playerRevealedMatrix);
    }

    /**
     * Makes a deep copy of the specified matrix
     * @param matrix Double[][] matrix to copy
     * @return Double[][] copy of the specified matrix
     */
    private Double[][] copyOfMatrix(Double[][] matrix) {
        if (matrix == null)
            return new Double[rows][cols];
        Double[][] copy = new Double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (matrix[i][j] != null)
                    copy[i][j] = matrix[i][j];
            }
        }
        return copy;
    }

    /**
     * Get the status for the game
     * @return int for the constant representing the status
     */
    public int getGameStatus() {
        if (playerRevealedMatrix == null)
            return GAME_INPROGRESS;
        boolean BOOM = false;
        boolean WON = true;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (!BOOM)
                    BOOM = gameMatrix[i][j] && playerRevealedMatrix[i][j] != null;
                if (WON && !gameMatrix[i][j])
                    WON = playerRevealedMatrix[i][j] != null;
            }
        }
        if (BOOM) {
            gameStats.stopGame();
            gameStats.gameStatus = GAME_LOST;
            return GAME_LOST;
        }
        if (WON) {
            gameStats.stopGame();
            gameStats.gameStatus = GAME_WON;
            return GAME_WON;
        }
        gameStats.gameStatus =GAME_INPROGRESS;
        return GAME_INPROGRESS;
    }

    /**
     * Print all matrix from the game.
     * @return String representation of all matrix
     */
    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("GameMatrix:\n");
        stringBuilder.append(stringifyMatrix(GAME_MATRIX));
        stringBuilder.append("\n" + "ProbabilityMatrix:\n");
        stringBuilder.append(stringifyMatrix(PROBABILITY_MATRIX));
        stringBuilder.append("\n" + "PlayerMatrix:\n");
        if (playerRevealedMatrix != null)
            stringBuilder.append(stringifyMatrix(PLAYER_MATRIX));
        else
            stringBuilder.append("Game has not started yet, sweep a location!");
        return stringBuilder.toString();
    }

    /**
     * Get a string representation of the selected matrix
     * @param matrix int constant for matrix selection
     * @return String representation of the selected matrix
     */
    public String stringifyMatrix(int matrix) {
        StringBuilder stringBuilder = new StringBuilder();
        if (matrix == PLAYER_MATRIX)
            if (playerRevealedMatrix != null)
                Arrays.stream(playerRevealedMatrix).map(doubles -> Arrays.toString(doubles) + "\n").forEach(stringBuilder::append);
            else
                stringBuilder.append("Game has not started yet, sweep a location!");
        if (matrix == GAME_MATRIX)
            Arrays.stream(gameMatrix).map(booleans -> Arrays.toString(booleans) + "\n").forEach(stringBuilder::append);
        if (matrix == PROBABILITY_MATRIX)
            Arrays.stream(probabilityMatrix).map(ints -> Arrays.toString(ints) + "\n").forEach(stringBuilder::append);
        return stringBuilder.toString();
    }
    public GameStatistics getGameStatistics() {
        return gameStats;
    }
}
