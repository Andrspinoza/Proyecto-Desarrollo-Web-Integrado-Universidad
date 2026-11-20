package com.utp.quesale.iam.infrastructure.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class AuthRateLimiterTest {
    private final AtomicLong now = new AtomicLong(1_000_000);
    private final Clock clock = new Clock() {
        @Override public java.time.ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
        @Override public Instant instant() { return Instant.ofEpochMilli(now.get()); }
    };

    @Test void addressIsBlockedAfterTheLimitAndFreedWhenTheWindowPasses() {
        var limiter = new AuthRateLimiter(clock, 3, 60, 5, 900);
        assertTrue(limiter.allowRequest("1.1.1.1"));
        assertTrue(limiter.allowRequest("1.1.1.1"));
        assertTrue(limiter.allowRequest("1.1.1.1"));
        assertFalse(limiter.allowRequest("1.1.1.1"));
        assertTrue(limiter.allowRequest("2.2.2.2"));
        now.addAndGet(61_000);
        assertTrue(limiter.allowRequest("1.1.1.1"));
    }

    @Test void emailIsBlockedAfterRepeatedFailuresIgnoringCaseAndClearedOnSuccess() {
        var limiter = new AuthRateLimiter(clock, 30, 300, 3, 900);
        limiter.recordFailure("A@uni.edu");
        limiter.recordFailure("a@uni.edu ");
        assertFalse(limiter.emailBlocked("a@uni.edu"));
        limiter.recordFailure("a@uni.edu");
        assertTrue(limiter.emailBlocked("A@UNI.EDU"));
        limiter.clearFailures("a@uni.edu");
        assertFalse(limiter.emailBlocked("a@uni.edu"));
    }

    @Test void emailBlockExpiresAfterTheWindow() {
        var limiter = new AuthRateLimiter(clock, 30, 300, 2, 900);
        limiter.recordFailure("a@uni.edu");
        limiter.recordFailure("a@uni.edu");
        assertTrue(limiter.emailBlocked("a@uni.edu"));
        now.addAndGet(901_000);
        assertFalse(limiter.emailBlocked("a@uni.edu"));
    }
}
