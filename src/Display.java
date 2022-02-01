import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;


public class Display extends JFrame
{
    final int size = 500;
    private Color background = new Color(195, 151, 106);

    public Display(){
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(size, size);
        this.setTitle("2D Game");

        GamePanel gamePanel = new GamePanel(background, size);
        this.add(gamePanel);
        this.pack();

        this.setLocationRelativeTo(null);
        this.setVisible(true);
        gamePanel.startGameThread();
    }
}
