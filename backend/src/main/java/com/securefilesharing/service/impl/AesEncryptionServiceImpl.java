package com.securefilesharing.service.impl;

import com.securefilesharing.service.EncryptionService;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.AtomicMoveNotSupportedException;
import java.security.SecureRandom;

@Service
public class AesEncryptionServiceImpl implements EncryptionService {

    private static final String AES = "AES";
    private static final String AES_GCM_NO_PADDING = "AES/GCM/NoPadding";

    // AES-256
    private static final int KEY_SIZE = 32;

    // 96-bit IV recommended for GCM
    private static final int IV_SIZE = 12;

    // 128-bit authentication tag
    private static final int TAG_SIZE = 128;

    @Override
    public void encryptFile(Path inputFile, Path outputFile, byte[] secretKey) throws Exception {
        if (secretKey == null || secretKey.length != KEY_SIZE) throw new IllegalArgumentException("AES-256 key required");

        // Generate a unique random IV for every encryption
        byte[] iv = new byte[IV_SIZE];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);

        SecretKeySpec keySpec = new SecretKeySpec(secretKey, AES);

        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_SIZE, iv);

        Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);

        cipher.init(
                Cipher.ENCRYPT_MODE,
                keySpec,
                gcmSpec);

        try (
                InputStream input = Files.newInputStream(inputFile);

                OutputStream output = Files.newOutputStream(outputFile)) {

            // Store IV at beginning of encrypted file.
            // IV is not secret.
            output.write(iv);

            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = input.read(buffer)) != -1) {

                byte[] encrypted = cipher.update(buffer, 0, bytesRead);

                if (encrypted != null) {
                    output.write(encrypted);
                }
            }

            byte[] finalBytes = cipher.doFinal();

            if (finalBytes != null) {
                output.write(finalBytes);
            }
        }
    }

    @Override
    public void decryptFile(Path inputFile, Path outputFile, byte[] secretKey)
            throws Exception {
        if (secretKey == null || secretKey.length != KEY_SIZE) throw new IllegalArgumentException("AES-256 key required");
        Path outputDirectory = outputFile.toAbsolutePath().getParent();
        Path temporaryOutput = Files.createTempFile(outputDirectory, "sfs-decrypt-", ".tmp");
        boolean moved = false;
        try (InputStream input = Files.newInputStream(inputFile)) {

            // Read IV from beginning of encrypted file
            byte[] iv = input.readNBytes(IV_SIZE);

            if (iv.length != IV_SIZE) {
                throw new IllegalArgumentException(
                        "Invalid encrypted file");
            }

            SecretKeySpec keySpec = new SecretKeySpec(secretKey, AES);

            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_SIZE, iv);

            Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);

            cipher.init(
                    Cipher.DECRYPT_MODE,
                    keySpec,
                    gcmSpec);

            try (OutputStream output = Files.newOutputStream(temporaryOutput)) {

                byte[] buffer = new byte[8192];
                int bytesRead;

                while ((bytesRead = input.read(buffer)) != -1) {

                    byte[] decrypted = cipher.update(buffer, 0, bytesRead);

                    if (decrypted != null) {
                        output.write(decrypted);
                    }
                }

                // GCM verifies the authentication tag here
                byte[] finalBytes = cipher.doFinal();

                if (finalBytes != null) {
                    output.write(finalBytes);
                }
            }
            try {
                Files.move(temporaryOutput, outputFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporaryOutput, outputFile, StandardCopyOption.REPLACE_EXISTING);
            }
            moved = true;
        } finally {
            if (!moved) Files.deleteIfExists(temporaryOutput);
        }
    }
}
