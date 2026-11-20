package com.campusbridge.backend.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PlanRateLimiter Tests")
class PlanRateLimiterTest {

    static class MutableClock extends Clock {
        private final AtomicLong millis;
        private final ZoneId zone;

        MutableClock(long initialMillis) {
            this.millis = new AtomicLong(initialMillis);
            this.zone = ZoneId.of("UTC");
        }

        void advanceMillis(long delta) {
            millis.addAndGet(delta);
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return Instant.ofEpochMilli(millis.get());
        }

        @Override
        public long millis() {
            return millis.get();
        }
    }

    @Test
    @DisplayName("Allows up to 10 requests within the hour and rejects the 11th request")
    void shouldPermitTenRequestsAndRejectEleventh() {
        MutableClock clock = new MutableClock(1_000_000L);
        PlanRateLimiter limiter = new PlanRateLimiter(10, 3600_000L, clock);
        String userId = "student-1";

        // First 10 requests must succeed
        for (int i = 1; i <= 10; i++) {
            assertTrue(limiter.tryAcquire(userId), "Request #" + i + " should be allowed");
        }

        // 11th request must be rejected
        assertFalse(limiter.tryAcquire(userId), "11th request within the hour must be rejected");
    }

    @Test
    @DisplayName("Sliding window expires after 1 hour allowing subsequent requests")
    void shouldAllowRequestsAfterWindowExpires() {
        MutableClock clock = new MutableClock(1_000_000L);
        PlanRateLimiter limiter = new PlanRateLimiter(10, 3600_000L, clock);
        String userId = "student-2";

        // Exhaust limit
        for (int i = 1; i <= 10; i++) {
            assertTrue(limiter.tryAcquire(userId));
        }
        assertFalse(limiter.tryAcquire(userId));

        // Advance clock by 1 hour + 1 second
        clock.advanceMillis(3600_001L);

        // Requests should now be accepted
        assertTrue(limiter.tryAcquire(userId), "Request should be permitted after 1 hour has elapsed");
    }
}
