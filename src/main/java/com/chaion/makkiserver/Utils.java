package com.chaion.makkiserver;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Utils {
    public static int[] getImageSize(File imageFile) throws IOException {
        BufferedImage image = ImageIO.read(imageFile);
        return new int[] {
                image.getWidth(), image.getHeight()
        };
    }
}
