package com.chaion.makkiiserver.modules.pokket.security;

import java.util.Base64;

/**
 * Define the auxiliary class for converting between cipher byte array to
 * string.
 */
public class CipherHelper {
    /**
     * Convert the cipher array to string.
     * 
     * @param cipher the cipher byte array.
     * @return the corresponding string.
     */
    public static String toString(byte[] cipher) {
        return Base64.getEncoder().encodeToString(cipher);
    }

    /**
     * Convert the cipher string to byte array.
     * 
     * @param cipher the cihper string.
     * @return the corresponding cipher array.
     */
    public static byte[] toCipher(String cipher) {
        return Base64.getDecoder().decode(cipher);
    }
}