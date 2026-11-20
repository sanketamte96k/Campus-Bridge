package com.campusbridge.backend.security;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PlanRateLimiter {

    private static final int DEFAULT_MAX_REQUESTS_PER_HOUR = 10;
    private static final long ONE_HOUR_MS = 3600_000L;

    private final int maxRequests;
    private final long windowDurationMs;
    private final Clock clock;
    private final ConcurrentHashMap<String, Deque<Long>> requestHistory = new ConcurrentHashMap<>();

    public PlanRateLimiter() {
        this(DEFAULT_MAX_REQUESTS_PER_HOUR, ONE_HOUR_MS, Clock.systemUTC());
    }

    public PlanRateLimiter(int maxRequests, long windowDurationMs, Clock clock) {
        this.maxRequests = maxRequests;
        this.windowDurationMs = windowDurationMs;
        this.clock = clock;
    }

    /**
     * Checks if a request from userId can proceed under the sliding window rate limit.
     * @param userId the requesting user's ID
     * @return true if permitted, false if rate limit exceeded
     */
    public boolean tryAcquire(String userId) {
        if (userId == null || userId.isBlank()) {
            return false;
        }

        long now = clock.millis();
        long windowStart = now - windowDurationMs;

        Deque<Long> timestamps = requestHistory.computeIfAbsent(userId, k -> new ArrayDeque<>());

        synchronized (timestamps) {
            // Evict expired entries outside the sliding window
            while (!timestamps.isEmpty() && timestamps.peekFirst() <= windowStart) {
                timestamps.pollFirst();
            }

            if (timestamps.size() < maxRequests) {
                timestamps.addLast(now);
                return true;
            } else {
                return false;
            }
        }
    }

    public void clear() {
        requestHistory.clear();
    }
}
