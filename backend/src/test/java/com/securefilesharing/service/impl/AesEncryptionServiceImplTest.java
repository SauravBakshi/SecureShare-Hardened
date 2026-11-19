package com.securefilesharing.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.*;

class AesEncryptionServiceImplTest {
    @TempDir
    Path tempDirectory;

    private final AesEncryptionServiceImpl encryption = new AesEncryptionServiceImpl();

    @Test
    void encryptsAndDecryptsWithAuthenticatedRandomKey() throws Exception {
        Path original = tempDirectory.resolve("original.bin");
        Path encrypted = tempDirectory.resolve("stored.enc");
        Path decrypted = tempDirectory.resolve("download.bin");
        byte[] content = new byte[128_000];
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(content);
        new SecureRandom().nextBytes(key);
        Files.write(original, content);

        encryption.encryptFile(original, encrypted, key);
        encryption.decryptFile(encrypted, decrypted, key);

        assertArrayEquals(content, Files.readAllBytes(decrypted));
    }

    @Test
    void rejectsTamperedCiphertextWithoutLeavingPlaintextOutput() throws Exception {
        Path original = tempDirectory.resolve("original.bin");
        Path encrypted = tempDirectory.resolve("stored.enc");
        Path output = tempDirectory.resolve("must-not-exist.bin");
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        Files.writeString(original, "confidential payload");
        encryption.encryptFile(original, encrypted, key);

        byte[] bytes = Files.readAllBytes(encrypted);
        bytes[bytes.length - 1] ^= 0x01;
        Files.write(encrypted, bytes);

        assertThrows(Exception.class, () -> encryption.decryptFile(encrypted, output, key));
        assertFalse(Files.exists(output));
        try (var files = Files.list(tempDirectory)) {
            assertFalse(files.anyMatch(path -> path.getFileName().toString().startsWith("sfs-decrypt-")));
        }
    }
}
