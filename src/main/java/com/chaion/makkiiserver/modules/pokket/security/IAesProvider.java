package com.chaion.makkiiserver.modules.pokket.security;

import java.security.NoSuchAlgorithmException;

public interface IAesProvider extends ICryptoProvider {

    /**
     * Create the AES key.
     * 
     * @throws NoSuchAlgorithmException if the algorithm is not supported.
     * @return the generated random key.
     */
    String createKey() throws NoSuchAlgorithmException;
}