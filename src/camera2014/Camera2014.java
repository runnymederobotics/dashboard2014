/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package camera2014;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.JApplet;
import javax.swing.JPanel;

/**
 *
 * @author 1310
 */
public class Camera2014 extends JApplet implements Runnable {

    public static final long FPS = 25;
    BufferedImage originalImage, filteredImage;
    BallFinder redBall = new BallFinder(true);
    BallFinder blueBall = new BallFinder(false);
    int width, height;
    int xRed, yRed, radiusRed;
    int xBlue, yBlue, radiusBlue;

    @Override
    public void init() {
        this.setSize(680, 240);

        add(new Camera2014.CustomPanel());

        (new Thread(this)).start();
    }

    class CustomPanel extends JPanel {

        @Override
        public void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            Graphics2D g2d = (Graphics2D) graphics;

            if (originalImage != null) {
                g2d.drawImage(originalImage, null, 0, 0);
                g2d.drawImage(filteredImage, null, width, 0);

                g2d.setColor(Color.white);
                g2d.drawOval(xRed - radiusRed, yRed - radiusRed, radiusRed * 2, radiusRed * 2);
                g2d.drawOval(xBlue - radiusBlue, yBlue - radiusBlue, radiusBlue * 2, radiusBlue * 2);
            }
        }
    }

    @Override
    public void run() {
        long lastRunTime = 0, now;
        while (true) {
            now = System.currentTimeMillis();
            //Only run a maximum of FPS times per second
            if (now - lastRunTime > 1000 / FPS) {
                try {
                    String axisIP = "http://10.13.10.20/jpg/image.jpg?resolution=320x240";
                    URL imageURL = new URL(axisIP);//"http://192.168.1.149:8080/shot.jpg");//C:\\Users\\Braden\\Desktop\\blue.jpg");

                    //Get the image from the URL
                    originalImage = ImageIO.read(imageURL);

                    width = originalImage.getWidth();
                    height = originalImage.getHeight();

                    redBall.setImageSize(width, height);
                    blueBall.setImageSize(width, height);
                    
                    redBall.reset();
                    blueBall.reset();

                    //Get the pixel data from the image
                    int[] pixels = originalImage.getRGB(0, 0, width, height, null, 0, width);
                    //Copy the pixel data so we can use it to make a filtered image
                    int[] filteredPixels = pixels.clone();

                    //Reset everything to 0
                    xRed = 0;
                    yRed = 0;
                    radiusRed = 0;

                    //Apply threshold
                    for (int i = 0; i < pixels.length; i++) {
                        int rgb = pixels[i];

                        //Walk through the image from left to right, then top to bottom
                        //This means we get horizontal "slices" of the image
                        int x = i % width, y = i / width;

                        if (redBall.update(pixels, filteredPixels, x, y, rgb)) {
                        } else if (blueBall.update(pixels, filteredPixels, x, y, rgb)) {
                        } else {
                            //If the pixel is outside our threshold, then don't draw it in the filtered image
                            filteredPixels[i] = 0;
                        }
                    }

                    //Record the position and radius of the red ball
                    xRed = redBall.getX();
                    yRed = redBall.getY();
                    radiusRed = redBall.getRadius();

                    //Record the position and radius of the red ball
                    xBlue = blueBall.getX();
                    yBlue = blueBall.getY();
                    radiusBlue = blueBall.getRadius();

                    //Create a new image to show the filters on
                    filteredImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                    filteredImage.setRGB(0, 0, width, height, filteredPixels, 0, width);

                    //Refresh the drawing surface
                    repaint();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            //long timeTaken = (System.currentTimeMillis() - now);
            //System.out.println("Algorithm time: " + timeTaken + " FPS: " + (1000 / timeTaken));
        }
    }
}

class BallFinder {

    public static final Color redColor = new Color(190, 60, 10)/*new Color(200, 70, 20)*/, blueColor = new Color(10, 100, 190);
    boolean colour;
    int imageWidth, imageHeight;
    int xLongest, yLongest;
    //int longestHorizontal, longestVertical;
    int longestRadius;
    int xFinishLast, yLast;

    public BallFinder(boolean colour) {
        this.colour = colour;

        reset();
    }

    public void setImageSize(int imageWidth, int imageHeight) {
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }
    
    public void reset() {
        xLongest = 0;
        yLongest = 0;
        longestRadius = 0;
        xFinishLast = 0;
        yLast = 0;
    }

    public int getX() {
        return xLongest;
    }

    public int getY() {
        return yLongest;
    }

    public int getRadius() {
        return longestRadius;//Math.min(longestHorizontal, longestVertical) / 2;
    }

    //colour -> red, !colour -> blue
    public boolean update(int pixels[], int filteredPixels[], int x, int y, int rgb) {
        if (checkThreshold(colour, rgb)) {
            //If we're in a different "chunk" of this horizontal slice, or if we've moved to the next line
            if (x > xFinishLast || y > yLast) {
                int xFinish, yFinish, yStart;
                //Start at the first x value of the current "chunk" of this horizontal slice
                //Walk through this chunk until we find a pixel outside of our threshold
                for (xFinish = x; xFinish < imageWidth - 1 && checkThreshold(colour, pixels[y * imageWidth + xFinish]); xFinish++) {
                }

                //We now have the left and right endpoints of this horizontal chunk

                //Find the midpoint of the horizontal chunk
                int xMid = (x + xFinish) / 2;

                //Start at the current y value, and walk down the image until we get outside the threshold
                //The x-value we're walking at is the midpoint we found earlier
                for (yFinish = y; yFinish < imageHeight - 1 && checkThreshold(colour, pixels[yFinish * imageWidth + xMid]); yFinish++) {
                }

                //Start at the current y value, and walk up the image until we get outside the threshold
                //The x-value we're walking at is the midpoint we found earlier
                for (yStart = y; yStart >= 0 && checkThreshold(colour, pixels[yStart * imageWidth + xMid]); yStart--) {
                }

                //Find the diameter of the horiztonal chunk
                int xLength = Math.abs(xFinish - x);
                //Find the length to the bottom and top of the ball
                int yLengthDown = Math.abs(yFinish - y), yLengthUp = Math.abs(y - yStart);

                //Take the smaller vertical length
                int yLength = Math.min(yLengthDown, yLengthUp);//yLengthDown + yLengthUp) / 2;

                //Take the average of the x-radius and y-radius
                //xLength is the diameter so divide it by 2
                int currentRadius = (xLength / 2 + yLength) / 2;

                //If we've found a new longest horizontal chunk then record it and its midpoint
                //If we've found a new longest vertical chunk then record it and its midpoint
                if (currentRadius > longestRadius) {
                    xLongest = xMid; //x is at the left of the ball, xMid is in the middle
                    yLongest = y; //y is in the middle of the ball
                    longestRadius = currentRadius;
                }

                //Record where the endpoint of the current chunk is
                //Record the current y-position of this chunk
                //These are used to know when to start looking for the next chunk
                //Either the next chunk will be on the same line with a higher x value, or the next line
                xFinishLast = xFinish;
                yLast = y;
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean checkThreshold(boolean colour, int rgb) {
        if (colour) {
            return checkRedThreshold(rgb);
        } else {
            return checkBlueThreshold(rgb);
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