/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package camera2013;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.JApplet;

/**
 *
 * @author 1310
 */
public class Camera2013 extends JApplet implements Runnable {

    public static final long FPS = 25;
    public static final Color redColor = new Color(200, 70, 20), blueColor = new Color(20, 70, 200);
    BufferedImage originalImage, filteredImage;
    int width, height;
    int maxRadius = 0, xPos = 0, yPos = 0;

    @Override
    public void init() {
        this.setSize(680, 240);
        (new Thread(this)).start();
    }

    @Override
    public void paint(Graphics graphics) {
        Graphics2D g2d = (Graphics2D) graphics;

        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        if (originalImage != null) {
            g2d.drawImage(originalImage, null, 0, 0);
            g2d.drawImage(filteredImage, null, width, 0);

            g2d.setColor(Color.white);
            g2d.drawOval(xPos - maxRadius, yPos - maxRadius, maxRadius * 2, maxRadius * 2);
        }
    }
    
    int longestHorizontal = 0, xLongest = 0;
    int longestVertical = 0, yLongest = 0;

    @Override
    public void run() {
        long lastRunTime = 0, now;
        while (true) {
            now = System.currentTimeMillis();
            if (now - lastRunTime > 1000 / FPS) {
                try {
                    URL imageURL = new URL("http://10.13.10.20/jpg/image.jpg?resolution=320x240");
                    originalImage = ImageIO.read(imageURL);

                    width = originalImage.getWidth();
                    height = originalImage.getHeight();

                    int[] pixels = originalImage.getRGB(0, 0, width, height, null, 0, width);
                    int[] filteredPixels = pixels.clone();
                    
                    xPos = 0;
                    yPos = 0;
                    maxRadius = 0;
                    xLongest = 0;
                    yLongest = 0;
                    longestHorizontal = 0;
                    longestVertical = 0;
                    
                    //Apply threshold
                    for (int i = 0; i < pixels.length; i++) {
                        int rgb = pixels[i];

                        int x = i % width, y = i / width;

                        if (checkRedThreshold(rgb)) {
                            int xFinish, yFinish;
                            for (xFinish = x; xFinish < width && checkRedThreshold(pixels[y * width + xFinish]); xFinish++) {
                            }
                            
                            int xMid = (x + xFinish) / 2;
                            
                            for (yFinish = y; yFinish < height && checkRedThreshold(pixels[yFinish * width + xMid]); yFinish++) {
                            }
                            
                            int yMid = (y + yFinish) / 2;
                            
                            int xLength = xFinish - x, yLength = yFinish - y;
                            
                            if (xLength > longestHorizontal) {
                                longestHorizontal = xLength;
                                xLongest = xMid;
                            }
                            
                            if (yLength > longestVertical) {
                                longestVertical = yLength;
                                yLongest = yMid;
                            }
                        } else {
                            filteredPixels[i] = 0;
                        }
                    }
                    
                    xPos = xLongest;
                    yPos = yLongest;
                    maxRadius = Math.max(longestHorizontal, longestVertical) / 2;

                    //Create a new image to show the filters on
                    filteredImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                    filteredImage.setRGB(0, 0, width, height, filteredPixels, 0, width);

                    repaint();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            long timeTaken = (System.currentTimeMillis() - now);
            System.out.println("Algorithm time: " + timeTaken + " FPS: " + (1000 / timeTaken));
        }
    }

    //This function returns whether or not the color specified by rgb is within the threshold specified for red
    private boolean checkRedThreshold(int rgb) {
        int r = rgb >> 16 & 0xFF, g = rgb >> 8 & 0xFF, b = rgb & 0xFF;
        return r > redColor.getRed() && g < redColor.getGreen() && b < redColor.getBlue();
    }

    //This function returns whether or not the color specified by rgb is within the threshold specified for blue
    private boolean checkBlueThreshold(int rgb) {
        int r = rgb >> 16 & 0xFF, g = rgb >> 8 & 0xFF, b = rgb & 0xFF;
        return r < blueColor.getRed() && g < blueColor.getGreen() && b > blueColor.getBlue();
    }
}
