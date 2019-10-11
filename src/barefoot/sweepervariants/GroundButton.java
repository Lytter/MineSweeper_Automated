package barefoot.sweepervariants;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class GroundButton extends JButton {
    public final static int GROUND_GRASS = 1;
    public final static int GROUND_BOMB = 2;
    public final static int GROUND_CLEAR = 3;
    public final static int GROUND_ADJECENT = 4;
    public final static int GROUND_FLAG = 5;

    int groundType;
    private int height;
    private int width;

    private static Image grass;
    private static Image bomb;
    private static Image flag;
    private static Image empty;

    GroundButton(Double type, int size) {
        loadResources();
        height = size;
        width = size;
        this.setForeground(Color.BLUE);
        this.setFont(new Font("Arial", Font.BOLD, size == 48 ? 22 : 22));
        updateType(type);
    }

    private void loadResources() {
        if (grass == null) {
            try {
                grass = ImageIO.read(getClass().getResource("/grass.png"));
                flag = ImageIO.read(getClass().getResource("/flag.png"));
                bomb = ImageIO.read(getClass().getResource("/mine8.png"));
                empty = ImageIO.read(getClass().getResource("/empty.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    int updateType(Double type) {
        int originalType = groundType;
        if (type == null) groundType = GROUND_GRASS;
        else {
            if (type >= 1.0 && type < Double.MAX_VALUE) groundType = GROUND_ADJECENT;
            else if (type == -1.0) groundType = GROUND_CLEAR;
            else if (type == 0) groundType = GROUND_BOMB;
            else if (type == Double.MAX_VALUE) groundType = GROUND_FLAG;
        }

        if (originalType == groundType)
            return groundType;
        Image img;
        switch (groundType) {
            case GROUND_GRASS:
                img = grass;
                break;
            case GROUND_CLEAR:
                img = empty;
                break;
            case GROUND_BOMB:
                img = bomb;
                break;
            case GROUND_FLAG:
                img = flag;
                break;
            default:
                img = null;
        }
        this.setText("");
        if (img != null) {
            Image dimg = img.getScaledInstance(width, height,
                    Image.SCALE_SMOOTH);
            this.setIcon(new ImageIcon(dimg));
        } else {
            this.setIcon(null);
            setMargin(new Insets(0, 0, 0, 0));
            if (type != null)
                this.setText("" + type.intValue());
        }
        this.setPreferredSize(new Dimension(width, height));
        this.repaint();
        return groundType;
    }
}
