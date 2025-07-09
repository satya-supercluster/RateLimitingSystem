package com.ratelimiter.algorithms;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.ratelimiter.core.RateLimitStorage;
import com.ratelimiter.core.RateLimitingAlgorithm;

/**
 * Token Bucket Algorithm Implementation
 * Advanced: Uses floating point arithmetic for precise token calculation
 */
public class TokenBucketAlgorithm implements RateLimitingAlgorithm {
    private final int capacity;
    private final double refillRate; // tokens per second
    private final RateLimitStorage storage;
    private final ConcurrentHashMap<String, ReentrantReadWriteLock> lockMap;
    
    public TokenBucketAlgorithm(int capacity, double refillRate, RateLimitStorage storage) {
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.storage = storage;
        this.lockMap = new ConcurrentHashMap<>();
    }
    
    @Override
    public boolean isAllowed(String key, long timestamp) {
        ReentrantReadWriteLock lock = lockMap.computeIfAbsent(key, k -> new ReentrantReadWriteLock());
        
        lock.writeLock().lock();
        try {
            String tokenKey = "tokens:" + key;
            String lastRefillKey = "lastRefill:" + key;
            
            double tokens = storage.get(tokenKey);
            long lastRefill = storage.get(lastRefillKey);
            
            if (lastRefill == 0) {
                // First request
                tokens = capacity;
                lastRefill = timestamp;
            } else {
                // Calculate tokens to add based on time elapsed
                double secondsElapsed = (timestamp - lastRefill) / 1000.0;
                double tokensToAdd = secondsElapsed * refillRate;
                tokens = Math.min(capacity, tokens + tokensToAdd);
            }
            
            if (tokens >= 1) {
                tokens -= 1;
                storage.set(tokenKey, (long)tokens, Duration.ofMinutes(10));
                storage.set(lastRefillKey, timestamp, Duration.ofMinutes(10));
                return true;
            }
            
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public void reset(String key) {
        storage.delete("tokens:" + key);
        storage.delete("lastRefill:" + key);
        lockMap.remove(key);
    }
    
    @Override
    public String getAlgorithmName() {
        return "TOKEN_BUCKET";
    }
    
    /**
     * Get the current token count for a key (for testing/monitoring)
     * 
     * @param key The key to check
     * @return Current token count
     */
    public long getCurrentTokens(String key) {
        return storage.get("tokens:" + key);
    }
}