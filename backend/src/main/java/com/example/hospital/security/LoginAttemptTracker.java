package com.example.hospital.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * In-memory brute-force protection for sign-in. After {@code maxFailures} wrong passwords for the same key
 * (client address + email) the key is blocked for {@code lockout}. A successful sign-in clears the counter.
 *
 * Plain Java (no Spring) so it can be unit tested. State is per server process: with several backend
 * instances, move this to a shared store such as Redis.
 */
public class LoginAttemptTracker {
    private static final int MAX_TRACKED_KEYS = 10_000;

    private static final class Entry {
        int failures;
        Instant firstFailure;
        Instant blockedUntil;
    }

    private final int maxFailures;
    private final Duration lockout;
    private final Clock clock;
    private final Map<String, Entry> entries = new HashMap<>();

    public LoginAttemptTracker(int maxFailures, Duration lockout, Clock clock) {
        if (maxFailures < 1) throw new IllegalArgumentException("maxFailures must be at least 1");
        this.maxFailures = maxFailures;
        this.lockout = lockout;
        this.clock = clock;
    }

    /** Seconds the caller must still wait, or 0 if sign-in attempts are allowed. */
    public synchronized long secondsBlocked(String key) {
        Entry e = entries.get(key);
        if (e == null) return 0;
        Instant now = clock.instant();
        if (e.blockedUntil != null && now.isBefore(e.blockedUntil)) {
            return Math.max(1, Duration.between(now, e.blockedUntil).toSeconds());
        }
        return 0;
    }

    public synchronized void recordFailure(String key) {
        Instant now = clock.instant();
        if (entries.size() >= MAX_TRACKED_KEYS) entries.values().removeIf(e -> isStale(e, now));
        Entry e = entries.get(key);
        if (e == null || isStale(e, now)) {
            e = new Entry();
            e.firstFailure = now;
            entries.put(key, e);
        }
        e.failures++;
        if (e.failures >= maxFailures) e.blockedUntil = now.plus(lockout);
    }

    public synchronized void recordSuccess(String key) {
        entries.remove(key);
    }

    /** An old entry no longer counts: its lock ran out, or its failures happened outside the window. */
    private boolean isStale(Entry e, Instant now) {
        if (e.blockedUntil != null) return !now.isBefore(e.blockedUntil);
        return now.isAfter(e.firstFailure.plus(lockout));
    }
}
