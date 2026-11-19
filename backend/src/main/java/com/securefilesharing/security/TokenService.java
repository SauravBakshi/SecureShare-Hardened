package com.securefilesharing.security;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

@Service
public class TokenService {
    private static final long LIFETIME_SECONDS = 3600;
    private final byte[] secret;

    @Autowired
    public TokenService() {
        this(System.getenv("SECURESHARE_TOKEN_SECRET"));
    }

    public TokenService(String configured) {
        if (configured == null || configured.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("SECURESHARE_TOKEN_SECRET must contain at least 32 random bytes");
        }
        this.secret = configured.getBytes(StandardCharsets.UTF_8);
    }

    public String issue(Long userId, String role) {
        if (userId == null || userId <= 0) throw new IllegalArgumentException("A positive user ID is required");
        if (!"USER".equals(role) && !"ADMIN".equals(role)) throw new IllegalArgumentException("Unsupported token role");
        long expiry = Instant.now().getEpochSecond() + LIFETIME_SECONDS;
        String payload = userId + ":" + role + ":" + expiry;
        String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        return encoded + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(sign(encoded));
    }

    public Claims verify(String token) {
        try {
            String[] parts = token.split("\\.", -1);
            if (parts.length != 2 || !MessageDigest.isEqual(sign(parts[0]), Base64.getUrlDecoder().decode(parts[1]))) return null;
            String[] claims = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8).split(":", -1);
            if (claims.length != 3) return null;
            long id = Long.parseLong(claims[0]);
            long expiry = Long.parseLong(claims[2]);
            if (id <= 0 || expiry <= Instant.now().getEpochSecond()) return null;
            if (!claims[1].equals("USER") && !claims[1].equals("ADMIN")) return null;
            return new Claims(id, claims[1]);
        } catch (RuntimeException ex) { return null; }
    }

    private byte[] sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) { throw new IllegalStateException("Token signing unavailable", ex); }
    }

    public record Claims(Long userId, String role) { }
}
