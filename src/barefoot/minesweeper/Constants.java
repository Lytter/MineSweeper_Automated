package barefoot.minesweeper;

/**
 * A class holding all the constants used by the game.
 */
public class Constants {
    //Constant for the game matrix
    public static final int GAME_MATRIX = 0;
    //Constant for the probability matrix
    public static final int PROBABILITY_MATRIX = 1;
    //Constant for player matrix
    public static final int PLAYER_MATRIX = 2;

    //Constants for the status of the game
    public static final int GAME_WON = 10;
    public static final int GAME_LOST = 20;
    public static final int GAME_INPROGRESS = 30;

    //Constants for SweeperBot implementations
    public static final int maxRounds = 20;
    public static final int waitForNextRound = 1;
    public static final int waitForNextMove = 500;

    //Constants for handling of MinesweeperGUI
    public static int[] GAME_EASY = {9, 9, 10};
    public static int[] GAME_MEDIUM = {16, 16, 40};
    public static int[] GAME_HARD = {16, 30, 99};
    public static int ACTION_SWEEP = 100;
    public static int ACTION_FLAG = 200;

    //Constants for custom board
    public static String CUSTOM_BOARD_FILE_EASY = ""; //"src//testBoardEasy.txt"; //file should be placed in src folder, * for bomb, x for clear
    public static String CUSTOM_BOARD_FILE_MEDIUM = ""; //file should be placed in src folder, * for bomb, x for clear
    public static String CUSTOM_BOARD_FILE_HARD = ""; //file should be placed in src folder, * for bomb, x for clear
}
