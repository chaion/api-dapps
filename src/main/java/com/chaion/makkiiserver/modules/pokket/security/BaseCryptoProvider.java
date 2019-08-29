package com.chaion.makkiiserver.modules.pokket.security;

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * Define the base class for crypto provider.
*/
public abstract class BaseCryptoProvider {
    /**
     * Set the RSA provider by default.
     */
    static {
        Security.addProvider(new BouncyCastleProvider());
    }
}