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
    int longestVertical = 0, longestX = 0;
    int longestHorizontal = 0, longestY = 0;

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

                    maxRadius = 0;
                    xPos = 0;
                    yPos = 0;

                    int xFinishLast = 0, yLast = 0;
                    
                    //Apply threshold
                    for (int i = 0; i < pixels.length; i++) {
                        int rgb = pixels[i];

                        int x = i % width, y = i / width;

                        if (checkRedThreshold(rgb)) {
                            if (x > xFinishLast || y != yLast) {
                                int xFinish = x;
                                while (xFinish >= 0 && xFinish < width && checkRedThreshold(pixels[y * width + xFinish])) {
                                    xFinish++;
                                }

                                int checkX = (x + xFinish) / 2;

                                int yFinish = y;
                                while (yFinish < height && checkRedThreshold(pixels[yFinish * width + checkX])) {
                                    yFinish++;
                                }

                                //filteredPixels[y * width + checkX] = 0xFFFFFF;

                                //int radius = searchCircle(pixels, checkX, y, true, 0, 0);
                                int radius = (yFinish - y) / 2;
                                if (radius > maxRadius) {
                                    maxRadius = radius;
                                    xPos = checkX;
                                    yPos = (yFinish + y) / 2;
                                }

                                xFinishLast = xFinish;
                                yLast = y;
                            }
                        } else {
                            filteredPixels[i] = 0;
                        }
                    }

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

    public int searchCircle(int[] pixels, int x, int y, boolean searchRed, int direction, int magnitude) {
        int i = y * width + x;

        if (i <= 0 || i >= pixels.length) {
            return 0;
        }

        //Rotate direction by 1 each time
        //right: 0b0001 down: 0b0010 left: 0b0100 up: 0b1000
        int curDirection = direction << 1;
        //If newDirection is out of range
        if (curDirection >= 0b10000) {
            //Back to default
            curDirection = 0b0001;
        }

        if (curDirection == 0) {
            curDirection = 0b0001;
        }

        int curMagnitude = 0, xCur = x, yCur = y;

        int rightMagnitude = magnitude & 0xFF;
        int downMagnitude = magnitude >> 8 & 0xFF;
        int leftMagnitude = magnitude >> 16 & 0xFF;
        int upMagnitude = magnitude >> 24 & 0xFF;

        switch (curDirection) {
            //Right
            case 0b0001:
                rightMagnitude += 2;
                curMagnitude = rightMagnitude;
                xCur += curMagnitude;
                break;
            //Down
            case 0b0010:
                downMagnitude += 2;
                curMagnitude = downMagnitude;
                yCur += curMagnitude;
                break;
            //Left
            case 0b0100:
                leftMagnitude += 2;
                curMagnitude = leftMagnitude;
                xCur -= curMagnitude;
                break;
            //Up
            case 0b1000:
                upMagnitude += 2;
                curMagnitude = upMagnitude;
                yCur -= curMagnitude;
                break;
        }

        int iCur = yCur * width + xCur;

        if (iCur < 0 || iCur >= pixels.length) {
            return curMagnitude;
        }

        int newMagnitude = (upMagnitude << 24) | (leftMagnitude << 16) | (downMagnitude << 8) | (rightMagnitude);

        int rgbCur = pixels[iCur];

        //This will tell us if the pixel we're looking at is in the range we are searching
        boolean pixelGood;
        if (searchRed) {
            pixelGood = checkRedThreshold(rgbCur);
        } else {
            pixelGood = checkBlueThreshold(rgbCur);
        }

        //If the pixel is out of range
        if (!pixelGood) {
            //Stop searching at this x, y position
            return curMagnitude;
        } else {
            //Continue with the breadth search
            return searchCircle(pixels, x, y, searchRed, curDirection, newMagnitude);
        }
    }
}
