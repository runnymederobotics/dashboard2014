package dashboard2014;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class CameraPanel extends JPanel implements Runnable {

    public static final long FPS = 25;
    public static final double FIELD_OF_VIEW = 55.0; //55.0 degrees
    public static final Color redColour = new Color(190, 60, 10), blueColour = new Color(50, 150, 10);
    public static final Color hotGoalColour = new Color(150, 120, 10);
    Dashboard2014 dashboard;
    BufferedImage originalImage, filteredImage;
    CircleFinder redBall = new CircleFinder(redColour, true, false, false);
    CircleFinder blueBall = new CircleFinder(blueColour, false, false, true);
    RectangleFinder hotGoal = new RectangleFinder(hotGoalColour, false, true, false);
    int width, height;

    public CameraPanel(Dashboard2014 dashboard) {
        this.dashboard = dashboard;

        setSize(680, 240);

        (new Thread(this)).start();
    }

    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D g2d = (Graphics2D) graphics;

        g2d.setColor(Color.black);
        graphics.fillRect(0, 0, getWidth(), getHeight());

        if (originalImage != null) {
            g2d.drawImage(originalImage, null, 0, 0);
            g2d.drawImage(filteredImage, null, width, 0);

            g2d.setColor(Color.cyan);
            redBall.draw(g2d);

            g2d.setColor(Color.red);
            blueBall.draw(g2d);

            g2d.setColor(Color.magenta);
            hotGoal.draw(g2d);
        }
    }

    @Override
    public void run() {
        long lastRunTime = 0, now;
        while (true) {
            now = System.currentTimeMillis();
            //Only run a maximum of FPS times per second
            if (now - lastRunTime > 1000 / FPS) {
                //Refresh the drawing surface
                repaint();

                try {
                    String axisIP = "http://10.13.10.20/jpg/image.jpg?resolution=320x240";
                    URL imageURL = new URL(axisIP);//"http://192.168.1.1:8080/shot.jpg");//C:\\Users\\Braden\\Desktop\\blue.jpg");

                    //Get the image from the URL
                    originalImage = ImageIO.read(imageURL);

                    width = originalImage.getWidth();
                    height = originalImage.getHeight();

                    redBall.setImageSize(width, height);
                    blueBall.setImageSize(width, height);
                    hotGoal.setImageSize(width, height);

                    redBall.reset();
                    blueBall.reset();
                    hotGoal.reset();

                    //Get the pixel data from the image
                    int[] pixels = originalImage.getRGB(0, 0, width, height, null, 0, width);
                    //Copy the pixel data so we can use it to make a filtered image
                    int[] filteredPixels = pixels.clone();

                    //Apply threshold
                    for (int i = 0; i < pixels.length; i++) {
                        int rgb = pixels[i];

                        //Walk through the image from left to right, then top to bottom
                        //This means we get horizontal "slices" of the image
                        int x = i % width, y = i / width;

                        if (redBall.doProcessing(pixels, filteredPixels, x, y, rgb)) {
                        } else if (blueBall.doProcessing(pixels, filteredPixels, x, y, rgb)) {
                        } else if (hotGoal.doProcessing(pixels, filteredPixels, x, y, rgb)) {
                        } else {
                            //If the pixel is outside our threshold, then don't draw it in the filtered image
                            filteredPixels[i] = 0;
                        }
                    }

                    //Create a new image to show the filters on
                    filteredImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                    filteredImage.setRGB(0, 0, width, height, filteredPixels, 0, width);
                } catch (Exception ex) {
                    //ex.printStackTrace();
                }
            }
            try {
                //System.out.println("red distance: " + redBall.getDistance());
                //System.out.println("blue distance: " + blueBall.getDistance());
            } catch (Exception e) {
            }
        }
    }
}
