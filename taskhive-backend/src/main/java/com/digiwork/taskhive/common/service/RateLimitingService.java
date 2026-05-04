package com.digiwork.taskhive.common.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
public class RateLimitingService {

    // Cache limits to prevent memory exhaustion if an attacker generates infinite random IPs/Emails
    private final Cache<String, Bucket> bucketCache = Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterAccess(Duration.ofMinutes(20))
            .build();

    public Bucket resolveBucket(String key) {
        return bucketCache.get(key, this::newBucket);
    }

    private Bucket newBucket(String key) {
        // Strict limit: 3 requests per 15 minutes per key
        Bandwidth limit = Bandwidth.builder()
                .capacity(3)
                .refillIntervally(3, Duration.ofMinutes(15))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}
