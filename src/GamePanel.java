import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.Random;

import static java.util.Map.entry;

public class GamePanel extends JPanel implements Runnable {
    Thread gameThread;

    int state; // 0 = main menu, 1 = game
    final int tileSize = 5;
    final int noiseMapSize = 2000;
    final int visableMapSize = 100;
    MapGenerator mapGenerator;

    int offsetX;
    int offsetY;
    int tideCount;
    int tidePosition;

    final boolean averageColors = false;
    final boolean tide = true; // water will go in and out
    final boolean elevationShadows = true;

    int FPS = 60; // FPS

    Random random = new Random();

    Map<Integer, Integer> biomeHeights = Map.ofEntries(
            entry(new Color(164,148,116).getRGB(), 0),
            entry(new Color(112, 164, 79).getRGB(), 1),
            entry(new Color(212,188,140).getRGB(), 1),
            entry(new Color(96,146,94).getRGB(), 1),
            entry(new Color(52,116,84).getRGB(), 1),
            entry(new Color(172, 113, 74).getRGB(), 2),
            entry(new Color(92, 135, 65).getRGB(), 2),
            entry(new Color(146, 74, 54).getRGB(), 3),
            entry(new Color(136,136,136).getRGB(), 3),
            entry(new Color(58, 22, 14).getRGB(), 4),
            entry(new Color(85,85,85).getRGB(), 4),
            entry(new Color(217, 215, 199).getRGB(), 5)
    );

    public GamePanel(Color background, int size){
        this.setPreferredSize(new Dimension(size, size));
        this.setBackground(background);
        this.setDoubleBuffered(true);

        mapGenerator = new MapGenerator(random.nextInt(-1000, 1000), noiseMapSize);
        mapGenerator.generateMap(0, 0);

        state = 1;
        tideCount = 0;
        tidePosition = 0;

        offsetX = (int) Math.round(noiseMapSize / 2d);
        offsetY = (int)Math.round(noiseMapSize / 2d);

        System.out.println(biomeHeights);
    }

    public void paintComponent(Graphics g){
        switch (state){
            case 0:
                paintMenu(g);
                break;
            case 1:
                paintMap(g);
                break;
        }

    }

    private void paintMap(Graphics g){
        super.paintComponent(g);

        Color[][] colors = mapGenerator.getColorMap();
        Graphics2D graphics = (Graphics2D) g;

        tideCount = (tideCount > 60)? 0 : tideCount + 1;

        if (tideCount == 0){
            if (tidePosition >= 3){
                tidePosition = 0;
            } else{
                tidePosition++;
            }
        }

        for (int y = 0; y < visableMapSize; y++){
            for (int x = 0; x < visableMapSize; x++){
                int baseX = x + (int)Math.round(offsetX);
                int baseY = y + (int)Math.round(offsetY);

                if (averageColors){
                    double R = 0;
                    double G = 0;
                    double B = 0;

                    R += colors[baseX - 1][baseY].getRed() * colors[baseX - 1][baseY].getRed();
                    G += colors[baseX - 1][baseY].getGreen() * colors[baseX - 1][baseY].getGreen();
                    B += colors[baseX - 1][baseY].getBlue() * colors[baseX - 1][baseY].getBlue();

                    R += colors[baseX + 1][baseY].getRed() * colors[baseX + 1][baseY].getRed() ;
                    G += colors[baseX + 1][baseY].getGreen() * colors[baseX + 1][baseY].getGreen();
                    B += colors[baseX + 1][baseY].getBlue() * colors[baseX + 1][baseY].getBlue();

                    R += colors[baseX][baseY - 1].getRed() * colors[baseX][baseY - 1].getRed();
                    G += colors[baseX][baseY - 1].getGreen() * colors[baseX][baseY - 1].getGreen();
                    B += colors[baseX][baseY - 1].getBlue() * colors[baseX][baseY - 1].getBlue();

                    R += colors[baseX][baseY + 1].getRed() * colors[baseX][baseY + 1].getRed();
                    G += colors[baseX][baseY + 1].getGreen() * colors[baseX][baseY + 1].getGreen();
                    B += colors[baseX][baseY + 1].getBlue() * colors[baseX][baseY + 1].getBlue();

                    R = Math.round(Math.sqrt(R / 4d));
                    G = Math.round(Math.sqrt(G / 4d));
                    B = Math.round(Math.sqrt(B / 4d));

                    graphics.setColor(new Color((int)R, (int)G, (int)B));

                    if (tide && colors[x + offsetX][y + offsetY].equals(new Color(65, 155, 161)) && tidePosition != 0) {
                        graphics.fillRect(x * tileSize, (y - 1) * tileSize, tileSize, tileSize);
                        graphics.fillRect((x - 1) * tileSize, y * tileSize, tileSize, tileSize);

                        if (tidePosition == 2) {
                            graphics.fillRect(x * tileSize, (y - 2) * tileSize, tileSize, tileSize);
                            graphics.fillRect((x - 2) * tileSize, y * tileSize, tileSize, tileSize);
                        }
                    }

                    if (elevationShadows){
                        int RGB1 = colors[baseX][baseY].getRGB();
                        int RGB2 = colors[baseX][baseY + 1].getRGB();
                        int RGB3 = colors[baseX + 1][baseY].getRGB();

                        if (biomeHeights.containsKey(RGB1) && biomeHeights.containsKey(RGB2) && biomeHeights.containsKey(RGB3)) {
                            int value1 = biomeHeights.get(RGB1);
                            int value2 = biomeHeights.get(RGB2);
                            int value3 = biomeHeights.get(RGB3);

                            if (value1 < value2 || value1 < value3) {
                                graphics.setColor(new Color((int) R, (int) G, (int) B).darker());
                            }
                        }
                    }
                } else{
                    if (tide){
                        Color waterColor = new Color(65, 155, 161);

                        if (colors[x + offsetX][y + offsetY].equals(waterColor) && tidePosition != 0){
                            graphics.setColor(waterColor);
                            graphics.fillRect(x * tileSize, (y - 1) * tileSize, tileSize, tileSize);
                            graphics.fillRect((x - 1) * tileSize, y * tileSize, tileSize, tileSize);

                            if (tidePosition == 2){
                                graphics.fillRect(x * tileSize, (y - 2) * tileSize, tileSize, tileSize);
                                graphics.fillRect((x - 2) * tileSize, y * tileSize, tileSize, tileSize);
                            }
                        } else {
                            graphics.setColor(colors[x + offsetX][y + offsetY]);
                        }

                    } else{
                        graphics.setColor(colors[x + offsetX][y + offsetY]);
                    }

                    if (elevationShadows){
                        int RGB1 = colors[baseX][baseY].getRGB();
                        int RGB2 = colors[baseX][baseY + 1].getRGB();
                        int RGB3 = colors[baseX - 1][baseY].getRGB();
                        int RGB4 = colors[baseX + 1][baseY].getRGB();
                        int RGB5 = colors[baseX][baseY - 1].getRGB();

                        if (biomeHeights.containsKey(RGB1) && biomeHeights.containsKey(RGB2) && biomeHeights.containsKey(RGB3) && biomeHeights.containsKey(RGB4) && biomeHeights.containsKey(RGB5)) {
                            int value1 = biomeHeights.get(RGB1);
                            int value2 = biomeHeights.get(RGB2);
                            int value3 = biomeHeights.get(RGB3);
                            int value4 = biomeHeights.get(RGB4);
                            int value5 = biomeHeights.get(RGB5);

                            if (value1 > value2 || value1 > value3 || value1 > value4 || value1 > value5) {
                                graphics.setColor(colors[baseX][baseY]);
                                graphics.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
                                graphics.setColor(new Color(0, 0, 0, 63));
                            }
                        }
                    }
                }

                graphics.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
            }
        }

        graphics.dispose();
    }

    private void paintMenu(Graphics g){
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 28));
        g2.drawString("The Wild West", 136, 100);

        JButton button1 = new JButton("Start");
        JButton button2 = new JButton("Options");
        JButton button3 = new JButton("Quit");
        this.add(button1);
        this.add(button2);
        this.add(button3);


        g2.dispose();
    }

    public void startGameThread(){
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void update(){


        if (Keyboard.isKeyPressed(87)){ // w
            // System.out.println("W Pressed");
            offsetY -= 1;
        }

        if (Keyboard.isKeyPressed(83)){ // s
            // System.out.println("S Pressed");
            offsetY += 1;
        }

        if (Keyboard.isKeyPressed(65)){ // a
            // System.out.println("A Pressed");
            offsetX -= 1;
        }

        if (Keyboard.isKeyPressed(68)){ // d
            //System.out.println("D Pressed");
            offsetX += 1;
        }

        clampOffset();
    }

    @Override
    public void run(){

        double drawInterval = 1000000000/FPS;
        double nextDrawTime = System.nanoTime() + drawInterval;

        while (gameThread != null){ // game loop

            update();

            repaint();

            try {
                double remainingTime = nextDrawTime - System.nanoTime();
                remainingTime = clamp0(remainingTime / 1000000);

                Thread.sleep((long)remainingTime);

                nextDrawTime += drawInterval;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private double clamp0(double value){
        if (value < 0){
            return 0;
        } else{
            return value;
        }
    }

    private void clampOffset(){
        if (offsetX > noiseMapSize - visableMapSize){
            offsetX = noiseMapSize - visableMapSize;
        } else if (offsetX < 0){
            offsetX = 0;
        }

        if (offsetY > noiseMapSize - visableMapSize){
            offsetY = noiseMapSize - visableMapSize;
        } else if (offsetY < 0){
            offsetY = 0;
        }
    }
}
