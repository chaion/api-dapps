package com.chaion.makkiiserver.modules.pokket.security;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Define the interface of encyption and decryption.
 */
public interface ICryptoProvider {
        /**
         * The bouncy castle provider string.
         */
        final String BouncyCastleProvider = "BC";

        /**
         * Get the key.
         * 
         * @return the key.
         */
        Key getKey();

        /**
         * Encrypt the content.
         * 
         * @param content The input content.
         * @return the encrypted byte array.
         * @throws NullPointerException      if the key file is not loaded.
         * @throws NoSuchPaddingException    if the padding format does not have.
         * @throws NoSuchProviderException   if the cipher provider does not have.
         * @throws NoSuchAlgorithmException  if the the cipher algorithm does not have.
         * @throws IllegalBlockSizeException if the block size is ilegal.
         * @throws BadPaddingException       if the padding is invalid.
         */
        byte[] encrypt(String content) throws NullPointerException, NoSuchPaddingException, NoSuchProviderException,
                        NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException;

        /**
         * Decrypt the content. For pokket client, it's just for verification.
         * 
         * @param code the encrypted content.
         * @return the content.
         * @throws NullPointerException               if the key file is not loaded.
         * @throws NoSuchPaddingException             if the padding format does not
         *                                            have.
         * @throws NoSuchProviderException            if the cipher provider does not
         *                                            have.
         * @throws NoSuchAlgorithmException           if the the cipher algorithm does
         *                                            not have.
         * @throws IllegalBlockSizeException          if the block size is ilegal.
         * @throws BadPaddingException                if the padding is invalid.
         * @throws InvalidAlgorithmParameterException
         */
        String decrypt(byte[] code) throws NullPointerException, NoSuchPaddingException, NoSuchProviderException,
                        NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
                        InvalidAlgorithmParameterException;
}