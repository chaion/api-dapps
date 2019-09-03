package com.chaion.makkiiserver;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Utils {
    /**
     * Get image width and height
     *
     * @param imageFile
     * @return
     * @throws IOException
     */
    public static int[] getImageSize(File imageFile) throws IOException {
        BufferedImage image = ImageIO.read(imageFile);
        return new int[] {
                image.getWidth(), image.getHeight()
        };
    }

    /**
     * Convert byte array to string
     *
     * @param src
     * @return
     */
    public static String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
}
