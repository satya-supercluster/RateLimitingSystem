package com.ratelimiter.algorithms;

import java.time.Duration;

import com.ratelimiter.core.RateLimitStorage;
import com.ratelimiter.core.RateLimitingAlgorithm;

/**
 * Sliding Window Counter Algorithm
 * Advanced: Combines fixed window efficiency with sliding window accuracy
 */
public class SlidingWindowCounterAlgorithm implements RateLimitingAlgorithm {
    private final int maxRequests;
    private final Duration timeWindow;
    private final RateLimitStorage storage;
    
    public SlidingWindowCounterAlgorithm(int maxRequests, Duration timeWindow, RateLimitStorage storage) {
        this.maxRequests = maxRequests;
        this.timeWindow = timeWindow;
        this.storage = storage;
    }
    
    @Override
    public boolean isAllowed(String key, long timestamp) {
        long windowSizeMs = timeWindow.toMillis();
        long currentWindow = timestamp / windowSizeMs;
        long previousWindow = currentWindow - 1;
        
        String currentKey = key + ":" + currentWindow;
        String previousKey = key + ":" + previousWindow;
        
        long currentCount = storage.get(currentKey);
        long previousCount = storage.get(previousKey);
        
        // Calculate sliding window count
        double timeInCurrentWindow = (timestamp % windowSizeMs) / (double) windowSizeMs;
        double estimatedCount = previousCount * (1 - timeInCurrentWindow) + currentCount;
        
        if (estimatedCount < maxRequests) {
            storage.increment(currentKey, 1, timeWindow.multipliedBy(2));
            return true;
        }
        
        return false;
    }
    
    @Override
    public void reset(String key) {
        // For a complete reset, we'd need to track all window keys
        // This is a simplified version that resets the base key
        storage.delete(key);
        
        // In a production implementation, you might want to track active windows
        // and reset all of them
        long currentTime = System.currentTimeMillis();
        long windowSizeMs = timeWindow.toMillis();
        long currentWindow = currentTime / windowSizeMs;
        
        storage.delete(key + ":" + currentWindow);
        storage.delete(key + ":" + (currentWindow - 1));
    }
    
    @Override
    public String getAlgorithmName() {
        return "SLIDING_WINDOW_COUNTER";
    }
    
    /**
     * Get the estimated current request count for a key (for testing/monitoring)
     * 
     * @param key The key to check
     * @return Estimated current request count
     */
    public double getEstimatedRequestCount(String key) {
        long timestamp = System.currentTimeMillis();
        long windowSizeMs = timeWindow.toMillis();
        long currentWindow = timestamp / windowSizeMs;
        long previousWindow = currentWindow - 1;
        
        String currentKey = key + ":" + currentWindow;
        String previousKey = key + ":" + previousWindow;
        
        long currentCount = storage.get(currentKey);
        long previousCount = storage.get(previousKey);
        
        double timeInCurrentWindow = (timestamp % windowSizeMs) / (double) windowSizeMs;
        return previousCount * (1 - timeInCurrentWindow) + currentCount;
    }
}