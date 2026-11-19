package com.securefilesharing.service;

public interface RsaEncryptionService {
    String encryptAesKey(byte[] aesKey) throws Exception;
    byte[] decryptAesKey(String encryptedAesKey) throws Exception;
}
