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
    public static final double FIELD_OF_VIEW = 55.0; //55.0 degrees
    public static final Color redColour = new Color(190, 60, 10), blueColour = new Color(50, 150, 10);
    public static final Color hotGoalColour = new Color(150, 120, 10);
    BufferedImage originalImage, filteredImage;
    CircleFinder redBall = new CircleFinder(redColour, true, false, false);
    CircleFinder blueBall = new CircleFinder(blueColour, false, false, true);
    RectangleFinder hotGoal = new RectangleFinder(hotGoalColour, false, true, false);
    int width, height;

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

                g2d.setColor(Color.cyan);
                redBall.draw(g2d);

                g2d.setColor(Color.red);
                blueBall.draw(g2d);

                g2d.setColor(Color.magenta);
                hotGoal.draw(g2d);
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

                    //Refresh the drawing surface
                    repaint();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            try {
                System.out.println("red distance: " + redBall.getDistance());
                System.out.println("blue distance: " + blueBall.getDistance());
            } catch (Exception e) {
            }
            //long timeTaken = (System.currentTimeMillis() - now);
            //System.out.println("Algorithm time: " + timeTaken + " FPS: " + (1000 / timeTaken));
        }
    }
}

abstract class Finder {

    Color colour;
    int imageWidth, imageHeight;
    int xLongest, yLongest;
    int longestWidth, longestHeight;
    int xFinishLast, yLast;
    boolean aboveRed, aboveGreen, aboveBlue;
    double actualWidthInches;

    public Finder(Color colour, boolean aboveRed, boolean aboveGreen, boolean aboveBlue) {
        this.colour = colour;

        /*int redColour = colour.getRed(), greenColour = colour.getGreen(), blueColour = colour.getBlue();

         int biggestComponent = Math.max(redColour, Math.max(greenColour, blueColour));

         aboveRed = biggestComponent == redColour;
         aboveGreen = biggestComponent == greenColour;
         aboveBlue = biggestComponent == blueColour;*/
        this.aboveRed = aboveRed;
        this.aboveGreen = aboveGreen;
        this.aboveBlue = aboveBlue;

        reset();
    }

    public void setImageSize(int imageWidth, int imageHeight) {
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    public void reset() {
        xLongest = 0;
        yLongest = 0;

        longestWidth = 0;
        longestHeight = 0;

        xFinishLast = 0;
        yLast = 0;
    }

    public int getX() {
        return xLongest;
    }

    public int getY() {
        return yLongest;
    }

    public int getWidth() {
        return longestWidth;
    }

    public int getHeight() {
        return longestHeight;
    }

    //Checks whether the specified pixel is in range for this finder
    protected boolean checkThreshold(int rgb) {
        int r = rgb >> 16 & 0xFF, g = rgb >> 8 & 0xFF, b = rgb & 0xFF;

        int redColour = colour.getRed(), greenColour = colour.getGreen(), blueColour = colour.getBlue();

        boolean redInRange, greenInRange, blueInRange;

        if (aboveRed) {
            redInRange = r > redColour;
        } else {
            redInRange = r < redColour;
        }

        if (aboveGreen) {
            greenInRange = g > greenColour;
        } else {
            greenInRange = g < greenColour;
        }

        if (aboveBlue) {
            blueInRange = b > blueColour;
        } else {
            blueInRange = b < blueColour;
        }

        return redInRange && greenInRange && blueInRange;
    }

    public boolean doProcessing(int pixels[], int filteredPixels[], int x, int y, int rgb) {
        if (checkThreshold(rgb)) {
            //If we're in a different "chunk" of this horizontal slice, or if we've moved to the next line
            if (x > xFinishLast || y > yLast) {
                int xFinish, yFinish, yStart;
                //Start at the first x value of the current "chunk" of this horizontal slice
                //Walk through this chunk until we find a pixel outside of our threshold
                for (xFinish = x; xFinish < imageWidth - 1 && checkThreshold(pixels[y * imageWidth + xFinish]); xFinish++) {
                }

                //We now have the left and right endpoints of this horizontal chunk

                //Find the midpoint of the horizontal chunk
                int xMid = (x + xFinish) / 2;

                //Start at the current y value, and walk down the image until we get outside the threshold
                //The x-value we're walking at is the midpoint we found earlier
                for (yFinish = y; yFinish < imageHeight - 1 && checkThreshold(pixels[yFinish * imageWidth + xMid]); yFinish++) {
                }

                //Start at the current y value, and walk up the image until we get outside the threshold
                //The x-value we're walking at is the midpoint we found earlier
                for (yStart = y; yStart >= 0 && checkThreshold(pixels[yStart * imageWidth + xMid]); yStart--) {
                }

                //Find the diameter of the horiztonal chunk
                int xLength = Math.abs(xFinish - x);
                //Find the length to the bottom and top of the ball
                int yLengthDown = Math.abs(yFinish - y), yLengthUp = Math.abs(y - yStart);

                //Determine if this sequence is the defining factor of the shape we are searching for
                checkIfLongest(xMid, y, xLength, yLengthDown, yLengthUp);

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

    //Determines the distance, in inches of the shape
    public double getDistance() {
        int x = getX(), y = getY();

        //Find the distance of the ball from the center of the image
        double deltaX = x - imageWidth / 2;
        //Approximate the angle from the middle of the image to the ball
        double angleToBall = Camera2014.FIELD_OF_VIEW / imageWidth * deltaX;
        //Use trig to find the distance from camera to ball, in pixels
        double distanceInPixels = deltaX / (Math.tan(Math.toRadians(angleToBall)));
        //Find ratio of inches to pixels
        final double INCHES_PER_PIXEL = actualWidthInches / getWidth();
        //Convert pixels to inches
        double distanceInInches = distanceInPixels * INCHES_PER_PIXEL;

        return distanceInInches;
    }

    //public abstract boolean doProcessing(int pixels[], int filteredPixels[], int x, int y, int rgb);
    public abstract void checkIfLongest(int xMid, int yMid, int xLength, int yLengthDown, int yLengthUp);

    public abstract void draw(Graphics2D g);
}

class CircleFinder extends Finder {

    public static final double BALL_DIAMETER_INCHES = 24.0;
    int longestRadius;

    public CircleFinder(Color colour, boolean aboveRed, boolean aboveGreen, boolean aboveBlue) {
        super(colour, aboveRed, aboveGreen, aboveBlue);
        actualWidthInches = BALL_DIAMETER_INCHES;
    }

    public int getRadius() {
        return longestRadius;//Math.min(longestHorizontal, longestVertical) / 2;
    }

    @Override
    public int getWidth() {
        return longestRadius * 2;
    }

    @Override
    public int getHeight() {
        return longestRadius * 2;
    }

    @Override
    public void reset() {
        super.reset();
        longestRadius = 0;
    }

    @Override
    public void checkIfLongest(int xMid, int yMid, int xLength, int yLengthDown, int yLengthUp) {
        //Take the smaller vertical length
        int yLength = Math.min(yLengthDown, yLengthUp);

        //Take the average of the x-radius and y-radius
        //xLength is the diameter so divide it by 2
        int currentRadius = (xLength / 2 + yLength) / 2;

        //If we've found a new longest horizontal chunk then record it and its midpoint
        //If we've found a new longest vertical chunk then record it and its midpoint
        if (currentRadius > longestRadius) {
            xLongest = xMid; //x is at the left of the ball, xMid is in the middle
            yLongest = yMid; //y is in the middle of the ball
            longestRadius = currentRadius;
        }
    }

    @Override
    public void draw(Graphics2D g) {
        int x = getX(), y = getY(), radius = getRadius();

        g.drawOval(x - radius, y - radius, radius * 2, radius * 2);
    }
}

class RectangleFinder extends Finder {

    public static final double HOT_GOAL_TARGET_WIDTH_INCHES = 23.5;

    public RectangleFinder(Color colour, boolean aboveRed, boolean aboveGreen, boolean aboveBlue) {
        super(colour, aboveRed, aboveGreen, aboveBlue);
        actualWidthInches = HOT_GOAL_TARGET_WIDTH_INCHES;
    }

    @Override
    public void checkIfLongest(int xMid, int yMid, int xLength, int yLengthDown, int yLengthUp) {
        //Take the smaller vertical length
        int yLength = yLengthDown + yLengthUp;

        //Take the average of the x-radius and y-radius
        //xLength is the diameter so divide it by 2
        int currentWidth = xLength, currentHeight = yLength;

        //If we've found a new longest horizontal chunk then record it and its midpoint
        //If we've found a new longest vertical chunk then record it and its midpoint
        if (currentWidth * currentHeight > longestWidth * longestHeight) {
            xLongest = xMid; //x is at the left of the ball, xMid is in the middle
            yLongest = yMid; //y is in the middle of the ball
            longestWidth = currentWidth;
            longestHeight = currentHeight;
        }
    }

    @Override
    public void draw(Graphics2D g) {
        int x = getX(), y = getY(), width = getWidth(), height = getHeight();

        g.drawRect(x - width / 2, y - height / 2, width, height);
    }
}