package com.securefilesharing.security;

import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimiter {
    private static final int MAX_ATTEMPTS = 10;
    private static final long WINDOW_SECONDS = 60;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    public boolean allow(String source) {
        long now = Instant.now().getEpochSecond();
        Window window = windows.compute(source, (key, current) -> {
            if (current == null || now - current.startedAt >= WINDOW_SECONDS) return new Window(now, 1);
            synchronized (current) { current.attempts++; }
            return current;
        });
        return window.attempts <= MAX_ATTEMPTS;
    }

    private static final class Window {
        private final long startedAt;
        private volatile int attempts;
        private Window(long startedAt, int attempts) { this.startedAt = startedAt; this.attempts = attempts; }
    }
}
