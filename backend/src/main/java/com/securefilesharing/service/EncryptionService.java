package com.securefilesharing.service;

import java.nio.file.Path;

public interface EncryptionService {

    void encryptFile(Path inputFile, Path outputFile, byte[] key) throws Exception;

    void decryptFile(Path inputFile, Path outputFile, byte[] key) throws Exception;
}
