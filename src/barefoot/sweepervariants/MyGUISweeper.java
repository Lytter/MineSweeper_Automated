package barefoot.sweepervariants;

import barefoot.minesweeper.Engine;
import barefoot.minesweeper.GameStatistics;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Arrays;

import static barefoot.minesweeper.Constants.*;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class MyGUISweeper {
    private JPanel mainPanel;
    private JLabel bombLabel;
    private JButton button1;
    private JPanel mineField;
    private JLabel roundLabel;

    private Engine engine;
    private int[] difficulty;
    private int buttonSize = 48;
    private Image grass, bomb, ballon, flag;
    //BOT
    private SweeperBot automationBot;

    /**
     * Starting point for any automatedSweeperBot.
     * Start a new botGame with:
     * MyGUISweeper.runAutomated(new RandomBotSweeper());
     * After that communication will be handled through the interface of SweeperBot
     * @param bot SweeperBot an instance of a SweeperBot
     * @see SweeperBot
     */
    public static void runAutomated(SweeperBot bot) {
        setUpGUI(bot);
    }

    /**
     * All actions of a SweeperBot implementation should be through this metod
     * @param command int as representeb by the Action constants in Constants
     * @param row int for row in the matrix
     * @param col int for column in the matrix
     * @see SweeperBot
     */
    public void takeAutomatedAction(int command, int row, int col) {
        if (command == ACTION_SWEEP)
            hitButtonAt(row, col);
        if (command == ACTION_FLAG)
            flagButton(row, col);
    }

    private void flagButton(int row, int col) {
        if (row >= difficulty[0] && col >= difficulty[1]
                || row < 0 || col < 0) {
            handleGameLoop();
            return;
        }
        GroundButton button = (GroundButton) mineField.getComponent(row * difficulty[0] + col);
        int type = button.updateType(Double.MAX_VALUE);
        engine.placeFlag(row, col);
        countBombs();
        handleGameLoop();
    }

    private void countBombs() {
        int bombs = difficulty[2];
        Double[][] board = engine.getPlayerRevealedMatrix();
        for (int i = 0; i < difficulty[0]; i++) {
            for (int j = 0; j < difficulty[1]; j++) {
                if (board[i][j] != null && board[i][j] == Double.MAX_VALUE)
                    bombs--;
            }
        }
        bombLabel.setText("" + Integer.valueOf(bombs));
    }

    public MyGUISweeper(SweeperBot bot) {
        try {
            grass = ImageIO.read(getClass().getResource("/grass.png"));
            flag = ImageIO.read(getClass().getResource("/flag.png"));
            bomb = ImageIO.read(getClass().getResource("/mine8.png"));
            ballon = ImageIO.read(getClass().getResource("/balloons.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        automationBot = bot;
        startNewGame();
    }

    //END BOT

    private void startNewGame() {
        roundLabel.setText("0");

        int result;
        if (automationBot == null) {
            Image img = bomb;
            result = JOptionPane.showOptionDialog(mainPanel, "Välj svårighetsgrad!", "Lets play minesweeper!"
                    , JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, new ImageIcon(img), new String[]{"Lätt", "Medel", "Svår"}, "Medel");
            if (result == 0)
                difficulty = GAME_EASY;
            if (result == 1)
                difficulty = GAME_MEDIUM;
            if (result == 2)
                difficulty = GAME_HARD;
        } else
            difficulty = automationBot.getDifficulty();
        engine = new Engine(difficulty[0], difficulty[1], difficulty[2]);
        bombLabel.setText("" + difficulty[2]);
        repaintGUI();
        if (automationBot != null)
            new Thread(() -> automationBot.takeAutomatedAction(engine.getPlayerRevealedMatrix(), this)).start();
    }

    private void hitButtonAt(int row, int col) {
        engine.sweepLocation(row, col);
        refreshAllButtons();
        handleGameLoop();
    }

    private void repaintGUI() {
        buttonSize = difficulty[0] == 9 ? 48 : 35;
        mineField.removeAll();
        mineField.setPreferredSize(new Dimension(difficulty[1] * buttonSize + 10, difficulty[0] * buttonSize + 25));
        Double[][] matrix = engine.getPlayerRevealedMatrix();
        for (int i = 0; i < difficulty[0]; i++) {
            for (int j = 0; j < difficulty[1]; j++) {
                Double type = matrix[i][j];
                GroundButton b = new GroundButton(type, buttonSize);
                b.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        super.mouseClicked(e);
                        if (engine.getGameStatus() != GAME_INPROGRESS)
                            return;
                        if (SwingUtilities.isRightMouseButton(e)) {
                            int type = b.updateType(Double.MAX_VALUE);
                            int pos = Arrays.asList(mineField.getComponents()).indexOf(b);
                            int row = pos / difficulty[1];
                            int col = pos % difficulty[1];
                            engine.placeFlag(row, col);
                            countBombs();
                        }
                    }
                });
                b.addActionListener((e) -> {
                    if (engine.getGameStatus() != GAME_INPROGRESS)
                        return;
                    if (b.groundType != GroundButton.GROUND_GRASS && b.groundType != GroundButton.GROUND_FLAG)
                        return;
                    int pos = Arrays.asList(mineField.getComponents()).indexOf(b);
                    int row = pos / difficulty[1];
                    int col = pos % difficulty[1];
                    hitButtonAt(row, col);
                });
                mineField.add(b);
            }
        }
        mineField.revalidate();
        mineField.repaint();
    }

    /**
     * Get the statistics for the current game
     * @return GameStatistics with all stats for the game
     */
    public GameStatistics getGameStatistics() {
        return engine.getGameStatistics();
    }

    private void handleGameLoop() {
        roundLabel.setText("" + engine.getGameStatistics().turnsTaken);
        if (engine.getGameStatus() == GAME_LOST) {
            if (automationBot == null) {
                Image img = bomb;
                int result = JOptionPane.showConfirmDialog(mainPanel, "Boooom!\n Vill du försöka igen?", "Game Over"
                        , JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, new ImageIcon(img));
                if (result == JOptionPane.YES_OPTION)
                    startNewGame();
            } else if (automationBot.playAgain(this)) {
                new Thread(this::startNewGame).start();
            } else
                JOptionPane.showMessageDialog(mainPanel, "Simuleringen är slutförd", "Klart",
                        JOptionPane.INFORMATION_MESSAGE, new ImageIcon(ballon));
        } else if (engine.getGameStatus() == GAME_WON) {
            if (automationBot == null) {
                Image img = ballon;
                int result = JOptionPane.showConfirmDialog(mainPanel, "Bravo!\n Vill du försöka igen?", "Game Over"
                        , JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, new ImageIcon(img));
                if (result == JOptionPane.YES_OPTION)
                    startNewGame();
            } else if (automationBot.playAgain(this)) {
                new Thread(this::startNewGame).start();
            }
        } else if (automationBot != null)
            new Thread(() -> automationBot.takeAutomatedAction(engine.getPlayerRevealedMatrix(), MyGUISweeper.this)).start();
    }

    private void refreshAllButtons() {
        Double[][] matrix = engine.getPlayerRevealedMatrix();
        for (int i = 0; i < mineField.getComponentCount(); i++) {
            GroundButton b = (GroundButton) mineField.getComponent(i);
            int row = i / difficulty[1];
            int col = i % difficulty[1];
            Double type = matrix[row][col];
            if (type != null && type == Double.MAX_VALUE)
                b.updateType(null);
            else
                b.updateType(type);
        }
    }

    private static void setUpGUI(SweeperBot automated) {
        //Skapa ditt fönster
        String namn = "Minesweeper";
        JFrame frame = new JFrame(namn);
        //Tala om att du vill kunna stänga ditt förnster med krysset i högra hörnet
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        //Ange storleken på ditt fönster och att det ska vara fast
        frame.setSize(1200, 720);
        frame.setResizable(false);
        //Positionera ditt fönster i mitten av skärmen
        frame.setLocationRelativeTo(null);

        //Skapa en instans av din den här klassen som hanterar din panel
        MyGUISweeper myForm = new MyGUISweeper(automated);
        //Lägg in din panel i programfönstret
        frame.setContentPane(myForm.mainPanel);
        //Visa programfönstret på skärmen
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        setUpGUI(null);
    }
}
