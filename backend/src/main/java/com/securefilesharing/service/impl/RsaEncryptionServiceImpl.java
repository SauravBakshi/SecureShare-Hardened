package com.securefilesharing.service.impl;

import com.securefilesharing.service.RsaEncryptionService;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.security.spec.MGF1ParameterSpec;
import java.security.interfaces.RSAKey;
import java.util.Base64;

@Service
public class RsaEncryptionServiceImpl implements RsaEncryptionService {

    private static final String RSA_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final OAEPParameterSpec OAEP = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);

    private final PublicKey publicKey;
    private final PrivateKey privateKey;

    public RsaEncryptionServiceImpl() throws Exception {

        String publicKeyString = System.getenv("SECURESHARE_RSA_PUBLIC_KEY");
        String privateKeyString = System.getenv("SECURESHARE_RSA_PRIVATE_KEY");

        if (publicKeyString == null || publicKeyString.isBlank()) {
            throw new IllegalStateException(
                    "SECURESHARE_RSA_PUBLIC_KEY environment variable is missing");
        }

        if (privateKeyString == null || privateKeyString.isBlank()) {
            throw new IllegalStateException(
                    "SECURESHARE_RSA_PRIVATE_KEY environment variable is missing");
        }

        this.publicKey = loadPublicKey(publicKeyString);
        this.privateKey = loadPrivateKey(privateKeyString);
        if (((RSAKey) publicKey).getModulus().bitLength() < 2048
                || ((RSAKey) privateKey).getModulus().bitLength() < 2048
                || !((RSAKey) publicKey).getModulus().equals(((RSAKey) privateKey).getModulus())) {
            throw new IllegalStateException("RSA key pair must match and use at least 2048-bit keys; 3072-bit keys are recommended");
        }
    }

    @Override
    public String encryptAesKey(byte[] aesKey) throws Exception {

        if (aesKey == null || aesKey.length != 32) {
            throw new IllegalArgumentException(
                    "AES key must be exactly 32 bytes");
        }

        Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);

        cipher.init(Cipher.ENCRYPT_MODE, publicKey, OAEP);

        byte[] encryptedKey = cipher.doFinal(aesKey);

        return Base64.getEncoder().encodeToString(encryptedKey);
    }

    @Override
    public byte[] decryptAesKey(String encryptedAesKey)
            throws Exception {

        if (encryptedAesKey == null || encryptedAesKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Encrypted AES key cannot be empty");
        }

        byte[] encryptedKey = Base64.getDecoder().decode(encryptedAesKey);

        Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);

        cipher.init(Cipher.DECRYPT_MODE, privateKey, OAEP);

        byte[] aesKey = cipher.doFinal(encryptedKey);

        if (aesKey.length != 32) {
            throw new IllegalArgumentException(
                    "Decrypted AES key is invalid");
        }

        return aesKey;
    }

    private PublicKey loadPublicKey(String key)
            throws Exception {

        byte[] decoded = Base64.getDecoder().decode(
                cleanKey(key));

        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return keyFactory.generatePublic(keySpec);
    }

    private PrivateKey loadPrivateKey(String key)
            throws Exception {

        byte[] decoded = Base64.getDecoder().decode(
                cleanKey(key));

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return keyFactory.generatePrivate(keySpec);
    }

    private String cleanKey(String key) {

        return key
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
    }
}
