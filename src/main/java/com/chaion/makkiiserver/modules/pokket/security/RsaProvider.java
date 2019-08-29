package com.chaion.makkiiserver.modules.pokket.security;

import static java.lang.String.format;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

public class RsaProvider extends BaseCryptoProvider implements IRsaProvider {

    /**
     * The RSA transform
     */
    public final String RsaTransform = "RSA/None/PKCS1Padding";

    /**
     * The signature algorithm
     */
    public final String SignatureAlgorithm = "SHA256withRSA";

    /**
     * The key.
     */
    private Key key;

    /**
     * Initialize the RSA encrypt/decrypt provider.
     */
    public RsaProvider() {
    }

    /**
     * Initialize the RSA encrypt/decrypt provider.
     * 
     * @param pemFile The PEM file path.
     */
    public RsaProvider(String pemFile) throws IOException {
        this.loadPemFile(pemFile);
    }

    @Override
    public byte[] encrypt(String content) throws NoSuchPaddingException, NoSuchProviderException,
            NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        if (this.key == null) {
            throw new InvalidKeyException("The key is not loaded");
        }

        Cipher cipher = Cipher.getInstance(RsaTransform, BouncyCastleProvider);
        cipher.init(Cipher.ENCRYPT_MODE, this.key);
        byte[] source = content.getBytes(StandardCharsets.UTF_8);
        return cipher.doFinal(source);
    }

    @Override
    public String decrypt(byte[] code) throws NoSuchPaddingException, NoSuchProviderException, NoSuchAlgorithmException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        if (this.key == null) {
            throw new InvalidKeyException("The key is not loaded");
        }

        Cipher cipher = Cipher.getInstance(RsaTransform, BouncyCastleProvider);
        cipher.init(Cipher.DECRYPT_MODE, this.key);
        byte[] res = cipher.doFinal(code);
        return new String(res, StandardCharsets.UTF_8);
    }

    @Override
    public void loadPemFile(String filename) throws IOException {
        File file = new File(filename);

        try (PEMParser pemReader = new PEMParser(new FileReader(file))) {
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(BouncyCastleProvider);
            Object obj = pemReader.readObject();
            if (obj instanceof SubjectPublicKeyInfo) {
                this.key = (Key) converter.getPublicKey((SubjectPublicKeyInfo) obj);
            } else if (obj instanceof PrivateKeyInfo) {
                this.key = converter.getPrivateKey((PrivateKeyInfo) obj);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void loadPemFile(InputStream stream) throws IOException {
        try (PEMParser pemReader = new PEMParser(new InputStreamReader(stream))) {
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider(BouncyCastleProvider);
            Object obj = pemReader.readObject();
            if (obj instanceof SubjectPublicKeyInfo) {
                this.key = (Key) converter.getPublicKey((SubjectPublicKeyInfo) obj);
            } else if (obj instanceof PrivateKeyInfo) {
                this.key = converter.getPrivateKey((PrivateKeyInfo)obj);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public boolean verify(String content, String signature)
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException {
        if (this.key == null) {
            throw new InvalidKeyException("The key is not loaded.");
        }

        if (!(this.key instanceof PublicKey)) {
            throw new InvalidKeyException(
                    format("Request the public key, but the current type is %s.", this.key.getClass().getName()));
        }

        Signature sig = Signature.getInstance(SignatureAlgorithm, BouncyCastleProvider);
        PublicKey publicKey = (PublicKey) this.key;
        sig.initVerify(publicKey);
        sig.update(content.getBytes(StandardCharsets.UTF_8));
        return sig.verify(CipherHelper.toCipher(signature));
    }

    @Override
    public String sign(String content)
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException {
        if (this.key == null) {
            throw new InvalidKeyException("The key is not loaded.");
        }

        if (!(this.key instanceof PrivateKey)) {
            throw new InvalidKeyException(
                    format("Request the private key, but the current type is %s.", this.key.getClass().getName()));
        }

        Signature sig = Signature.getInstance(SignatureAlgorithm, BouncyCastleProvider);
        PrivateKey privateKey = (PrivateKey) this.key;
        sig.initSign(privateKey);
        sig.update(content.getBytes(StandardCharsets.UTF_8));
        return CipherHelper.toString(sig.sign());
    }

    @Override
    public Key getKey() {
        return this.key;
    }
}