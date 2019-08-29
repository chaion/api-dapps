package com.chaion.makkiiserver.modules.pokket.security;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;

/**
 * Define the interface to encrypt/decrypt the content via RSA.
 * 
 * @author Pokket.com
 * @version 1.0
 */
public interface IRsaProvider extends ICryptoProvider {
        /**
         * Load the PEM key file.
         * 
         * @param file The PEM file.
         * @throws IOException if the PEM file does not exist.
         */
        void loadPemFile(String file) throws IOException;
        void loadPemFile(InputStream stream) throws IOException;

        /**
         * Sign the content.
         * 
         * @param content the content byte array.
         * @return the signature of the content.
         * @throws InvalidKeyException      if the key is invalid or the key is not a
         *                                  private key.
         * @throws NoSuchAlgorithmException if the algorithm is not supported.
         * @throws NoSuchProviderException  if the provider does not exist.
         * @throws SignatureException       if the signature is invalid.
         */
        String sign(String content) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException,
                        SignatureException;

        /**
         * Verify the content with the signature.
         * 
         * @param content   the content byte array.
         * @param signature the signature byte array.
         * @return True if the signature is valid. Otherwise, false.
         * @throws InvalidKeyException      if the key is invalid or the key is not a
         *                                  public key.
         * @throws NoSuchAlgorithmException if the algorithm is not supported.
         * @throws NoSuchProviderException  if the provider does not exist.
         * @throws SignatureException       if the signature is invalid.
         */
        boolean verify(String content, String signature) throws InvalidKeyException, NoSuchAlgorithmException,
                        NoSuchProviderException, SignatureException;
}
