package com.securefilesharing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Secure File Sharing Application - Phase 1
 * Note: Cryptographic modules (AES, RSA, SHA-3, HMAC, digital signatures)
 * are NOT implemented in this phase. This is the base infrastructure.
 */
@SpringBootApplication
public class SecureFileSharingApplication {
    public static void main(String[] args) {
        SpringApplication.run(SecureFileSharingApplication.class, args);
    }
}
