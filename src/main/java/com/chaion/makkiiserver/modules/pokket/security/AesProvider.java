package com.chaion.makkiiserver.modules.pokket.security;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.chaion.makkiiserver.modules.pokket.security.utils.RandomString;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class AesProvider extends BaseCryptoProvider implements IAesProvider {
    /**
     * The AES tag for AES security provider.
     */
    private final String AesTag = "AES/CBC/PKCS7Padding";

    /**
     * The SHA-256 tag.
     */
    private final String SHA256Tag = "SHA-256";

    /**
     * The key size of AES.
     */
    private final int KeySize = 128;

    /**
     * The size of initial vector.
     */
    private final int IvSize = 16;

    /**
     * The AES key.
     */
    private Key key;

    /**
     * The digist store.
     */
    private MessageDigest digest;

    /**
     * The random string generator.
     */
    private RandomString randomString;

    /**
     * Initialize a new instance of AES encryption provider.
     */
    public AesProvider() {
        try {
            this.digest = MessageDigest.getInstance(SHA256Tag);
            this.randomString = new RandomString(this.KeySize);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public AesProvider(String keyString) {
        this();
        this.loadKey(keyString);
    }

    @Override
    public Key getKey() {
        return this.key;
    }

    @Override
    public byte[] encrypt(String content) throws NullPointerException, NoSuchPaddingException, NoSuchProviderException,
            NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        if (this.key == null) {
            throw new NullPointerException("The key is not loaded");
        }

        Cipher cipher = Cipher.getInstance(AesTag, BouncyCastleProvider);
        cipher.init(Cipher.ENCRYPT_MODE, this.key);
        byte[] iv = cipher.getIV();
        byte[] encrypted = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
        try {
            return concat(iv, encrypted);
        } catch (IOException e) {
        }

        return null;
    }

    @Override
    public String decrypt(byte[] code) throws NullPointerException, NoSuchPaddingException, NoSuchProviderException,
            InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {
        if (this.key == null) {
            throw new NullPointerException("The key is not loaded");
        }

        byte[] iv = new byte[IvSize];
        byte[] encrypted = new byte[code.length - IvSize];
        System.arraycopy(code, 0, iv, 0, IvSize);
        System.arraycopy(code, IvSize, encrypted, 0, code.length - IvSize);
        IvParameterSpec spec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance(AesTag, BouncyCastleProvider);

        cipher.init(Cipher.DECRYPT_MODE, this.key, spec);
        return new String(cipher.doFinal(encrypted));
    }

    @Override
    public String createKey() throws NoSuchAlgorithmException {
        String k = this.randomString.generateString();
        this.loadKey(k);
        return k;
    }

    private void loadKey(String k) {
        byte[] hash = this.digest.digest(k.getBytes(StandardCharsets.UTF_8));
        this.key = new SecretKeySpec(hash, AesTag);
    }

    private static byte[] concat(byte[]... items) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (byte[] item : items) {
            baos.write(item);
        }

        return baos.toByteArray();
    }
}