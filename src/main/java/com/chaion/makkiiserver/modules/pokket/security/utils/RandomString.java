package com.chaion.makkiiserver.modules.pokket.security.utils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Define the class to generate the random string.
 */
public class RandomString {

    /**
     * Define the allowed characters for generated random string.
     */
    private final String AllowedChars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * The string length;
     */
    private int length;

    public RandomString(int length) {
        this.length = length;
    }

    /**
     * Generate the random string.
     * 
     * @return the generated random string.
     * @throws NoSuchAlgorithmException if the algorithm doesn't support.
     */
    public String generateString() throws NoSuchAlgorithmException {
        SecureRandom random = SecureRandom.getInstanceStrong();
        return random.ints(this.length, 0, this.AllowedChars.length()).mapToObj(c -> this.AllowedChars.charAt(c))
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
    }
}