/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package camera2013;

/**
 *
 * @author 1310
 */
public class temp {
    /*
     * public int searchCircle(int[] pixels, int x, int y, boolean searchRed, int direction, int magnitude) {
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
     * 
     */
}
