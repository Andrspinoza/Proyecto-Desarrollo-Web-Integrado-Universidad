package com.utp.quesale.iam.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AuthRateLimiter {
    private static final int CLEANUP_THRESHOLD = 10_000;

    private final Clock clock;
    private final int maxRequestsPerAddress;
    private final long addressWindowMillis;
    private final int maxFailuresPerEmail;
    private final long emailWindowMillis;
    private final Map<String, Deque<Long>> requests = new ConcurrentHashMap<>();
    private final Map<String, Deque<Long>> failures = new ConcurrentHashMap<>();

    public AuthRateLimiter(Clock clock,
                           @Value("${app.auth.rate-limit.address-max:30}") int maxRequestsPerAddress,
                           @Value("${app.auth.rate-limit.address-window-seconds:300}") long addressWindowSeconds,
                           @Value("${app.auth.rate-limit.email-max-failures:8}") int maxFailuresPerEmail,
                           @Value("${app.auth.rate-limit.email-window-seconds:900}") long emailWindowSeconds) {
        this.clock = clock;
        this.maxRequestsPerAddress = maxRequestsPerAddress;
        this.addressWindowMillis = addressWindowSeconds * 1000;
        this.maxFailuresPerEmail = maxFailuresPerEmail;
        this.emailWindowMillis = emailWindowSeconds * 1000;
    }

    public boolean allowRequest(String address) {
        return register(requests, address, addressWindowMillis, maxRequestsPerAddress);
    }

    public boolean emailBlocked(String email) {
        Deque<Long> attempts = failures.get(normalize(email));
        if (attempts == null) return false;
        synchronized (attempts) {
            evict(attempts, emailWindowMillis);
            return attempts.size() >= maxFailuresPerEmail;
        }
    }

    public void recordFailure(String email) {
        cleanup(failures, emailWindowMillis);
        Deque<Long> attempts = failures.computeIfAbsent(normalize(email), key -> new ArrayDeque<>());
        synchronized (attempts) {
            evict(attempts, emailWindowMillis);
            attempts.addLast(clock.millis());
        }
    }

    public void clearFailures(String email) {
        failures.remove(normalize(email));
    }

    public long addressRetryAfterSeconds() {
        return Math.max(1, addressWindowMillis / 1000);
    }

    private boolean register(Map<String, Deque<Long>> store, String key, long windowMillis, int max) {
        cleanup(store, windowMillis);
        Deque<Long> hits = store.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (hits) {
            evict(hits, windowMillis);
            if (hits.size() >= max) return false;
            hits.addLast(clock.millis());
            return true;
        }
    }

    private void evict(Deque<Long> hits, long windowMillis) {
        long limit = clock.millis() - windowMillis;
        while (!hits.isEmpty() && hits.peekFirst() <= limit) hits.pollFirst();
    }

    private void cleanup(Map<String, Deque<Long>> store, long windowMillis) {
        if (store.size() < CLEANUP_THRESHOLD) return;
        store.entrySet().removeIf(entry -> {
            synchronized (entry.getValue()) {
                evict(entry.getValue(), windowMillis);
                return entry.getValue().isEmpty();
            }
        });
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
