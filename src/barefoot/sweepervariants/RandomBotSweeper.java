package barefoot.sweepervariants;

import barefoot.sweepervariants.gui.MyGUISweeper;

import java.util.Random;

import static barefoot.minesweeper.Constants.*;

/**
 * This bot will randomly sweep locations and sometimes flag a location
 * Totally useless at playing the game, but informative in how to make a bot.
 */
public class RandomBotSweeper implements SweeperBot{
    public static void main(String[] args) {
        MyGUISweeper.runAutomated(new RandomBotSweeper());
    }

    private int playedRounds = 1;

    @Override
    public int[] getDifficulty() {
        int difficulty = new Random().nextInt(3);
        switch (difficulty) {
            case 0:
                return GAME_EASY;
            case 1:
                return GAME_MEDIUM;
            case 2:
                return GAME_HARD;
        }
        return GAME_EASY;
    }

    @Override
    public boolean playAgain(MyGUISweeper game) {
        try {
            Thread.sleep(waitForNextRound);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(game.getGameStatistics());
        return playedRounds++ < maxRounds;
    }

    @Override
    public void takeAutomatedAction(Double[][] playerRevealedBoard, MyGUISweeper game) {
        try {
            Thread.sleep(waitForNextMove);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int row, col;
        do {
            Random random = new Random();
            row = random.nextInt(playerRevealedBoard.length);
            col = random.nextInt(playerRevealedBoard[row].length);
        } while (playerRevealedBoard[row][col] != null);
        boolean makeFlag = new Random().nextBoolean();
        if (makeFlag)
            game.takeAutomatedAction(ACTION_SWEEP, row, col);
        else
            game.takeAutomatedAction(ACTION_FLAG, row, col);
    }
}
