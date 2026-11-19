package com.securefilesharing.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {
    private final TokenService service = new TokenService("unit-test-signing-secret-that-is-long-enough");

    @Test
    void issuesAndVerifiesTokenClaims() {
        String token = service.issue(42L, "USER");
        TokenService.Claims claims = service.verify(token);

        assertNotNull(claims);
        assertEquals(42L, claims.userId());
        assertEquals("USER", claims.role());
    }

    @Test
    void rejectsTamperedTokenAndInvalidRole() {
        String token = service.issue(42L, "USER");
        String tampered = token.substring(0, token.length() - 1) + (token.endsWith("A") ? "B" : "A");

        assertNull(service.verify(tampered));
        assertThrows(IllegalArgumentException.class, () -> service.issue(42L, "ROOT"));
    }

    @Test
    void refusesShortSigningSecret() {
        assertThrows(IllegalStateException.class, () -> new TokenService("short"));
    }
}
