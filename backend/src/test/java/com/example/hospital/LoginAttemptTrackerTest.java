package com.example.hospital;

import com.example.hospital.security.LoginAttemptTracker;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginAttemptTrackerTest {
    /** A clock the test can move forward. */
    static class MutableClock extends Clock {
        Instant now = Instant.parse("2026-10-12T09:00:00Z");
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    }

    private final MutableClock clock = new MutableClock();
    private final LoginAttemptTracker tracker = new LoginAttemptTracker(3, Duration.ofMinutes(15), clock);

    @Test
    void blocksAfterTooManyFailures() {
        tracker.recordFailure("a");
        tracker.recordFailure("a");
        assertThat(tracker.secondsBlocked("a")).isZero();
        tracker.recordFailure("a");
        assertThat(tracker.secondsBlocked("a")).isEqualTo(900);
    }

    @Test
    void otherKeysAreNotAffected() {
        for (int i = 0; i < 3; i++) tracker.recordFailure("a");
        assertThat(tracker.secondsBlocked("b")).isZero();
    }

    @Test
    void blockExpiresAndCounterRestarts() {
        for (int i = 0; i < 3; i++) tracker.recordFailure("a");
        clock.now = clock.now.plusSeconds(901);
        assertThat(tracker.secondsBlocked("a")).isZero();
        tracker.recordFailure("a");
        assertThat(tracker.secondsBlocked("a")).isZero();
    }

    @Test
    void successfulLoginClearsFailures() {
        tracker.recordFailure("a");
        tracker.recordFailure("a");
        tracker.recordSuccess("a");
        tracker.recordFailure("a");
        tracker.recordFailure("a");
        assertThat(tracker.secondsBlocked("a")).isZero();
    }

    @Test
    void failuresOutsideTheWindowDoNotCount() {
        tracker.recordFailure("a");
        tracker.recordFailure("a");
        clock.now = clock.now.plus(Duration.ofMinutes(16));
        tracker.recordFailure("a");
        assertThat(tracker.secondsBlocked("a")).isZero();
    }

    @Test
    void rejectsInvalidLimit() {
        assertThatThrownBy(() -> new LoginAttemptTracker(0, Duration.ofMinutes(1), clock))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
