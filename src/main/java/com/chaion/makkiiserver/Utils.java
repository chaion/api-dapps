package com.chaion.makkiiserver;

import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.UTF_8;

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

    public static String resource2String(Resource resource) throws IOException {
        Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8);
        return FileCopyUtils.copyToString(reader);
    }

    public static String inputStream2String(InputStream in) throws IOException {
        StringBuilder s = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader
                (in, Charset.forName(StandardCharsets.UTF_8.name())))) {
            int c = 0;
            while ((c = reader.read()) != -1) {
                s.append((char) c);
            }
        }
        return s.toString();
    }
}
