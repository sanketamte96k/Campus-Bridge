package com.campusbridge.backend.service;

import com.campusbridge.backend.dto.LearningPlanDto;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class PlanCache {

    private static final int MAX_ENTRIES = 500;
    private static final long TWENTY_FOUR_HOURS_MS = 24 * 3600_000L;

    private final int capacity;
    private final long ttlMs;
    private final Clock clock;
    private final Map<String, CacheEntry> cache;

    private static class CacheEntry {
        final LearningPlanDto plan;
        final long expiresAt;

        CacheEntry(LearningPlanDto plan, long expiresAt) {
            this.plan = plan;
            this.expiresAt = expiresAt;
        }
    }

    public PlanCache() {
        this(MAX_ENTRIES, TWENTY_FOUR_HOURS_MS, Clock.systemUTC());
    }

    public PlanCache(int capacity, long ttlMs, Clock clock) {
        this.capacity = capacity;
        this.ttlMs = ttlMs;
        this.clock = clock;
        this.cache = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> eldest) {
                return size() > PlanCache.this.capacity;
            }
        };
    }

    public static String computeKey(String userId, String jobId, List<String> studentSkills, List<String> missingSkills) {
        String studentSkillsStr = studentSkills != null ? String.join(",", studentSkills) : "";
        String missingSkillsStr = missingSkills != null ? String.join(",", missingSkills) : "";
        String raw = userId + ":" + jobId + ":" + studentSkillsStr + "|" + missingSkillsStr;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(raw.hashCode());
        }
    }

    public synchronized LearningPlanDto get(String key) {
        if (key == null) {
            return null;
        }

        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return null;
        }

        if (clock.millis() >= entry.expiresAt) {
            cache.remove(key);
            return null;
        }

        return entry.plan;
    }

    public synchronized void put(String key, LearningPlanDto plan) {
        if (key == null || plan == null) {
            return;
        }
        cache.put(key, new CacheEntry(plan, clock.millis() + ttlMs));
    }

    public synchronized void clear() {
        cache.clear();
    }

    public synchronized int size() {
        return cache.size();
    }
}
