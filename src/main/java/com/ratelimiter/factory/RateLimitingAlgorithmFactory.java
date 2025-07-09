package com.ratelimiter.factory;

import com.ratelimiter.algorithms.SlidingWindowCounterAlgorithm;
import com.ratelimiter.algorithms.SlidingWindowLogAlgorithm;
import com.ratelimiter.algorithms.TokenBucketAlgorithm;
import com.ratelimiter.core.RateLimitRule;
import com.ratelimiter.core.RateLimitStorage;
import com.ratelimiter.core.RateLimitingAlgorithm;

/**
 * Algorithm Factory
 * Factory Pattern for creating rate limiting algorithms
 */
public class RateLimitingAlgorithmFactory {
    
    /**
     * Create a rate limiting algorithm based on the specified type
     * 
     * @param type The algorithm type (TOKEN_BUCKET, SLIDING_WINDOW_LOG, SLIDING_WINDOW_COUNTER)
     * @param rule The rate limit rule configuration
     * @param storage The storage implementation to use
     * @return The created algorithm instance
     * @throws IllegalArgumentException if the algorithm type is unknown
     */
    public static RateLimitingAlgorithm createAlgorithm(String type, RateLimitRule rule, RateLimitStorage storage) {
        switch (type.toUpperCase()) {
            case "TOKEN_BUCKET":
                double refillRate = rule.getMaxRequests() / (double) rule.getTimeWindow().getSeconds();
                return new TokenBucketAlgorithm(rule.getMaxRequests(), refillRate, storage);
            
            case "SLIDING_WINDOW_LOG":
                return new SlidingWindowLogAlgorithm(rule.getMaxRequests(), rule.getTimeWindow(), storage);
            
            case "SLIDING_WINDOW_COUNTER":
                return new SlidingWindowCounterAlgorithm(rule.getMaxRequests(), rule.getTimeWindow(), storage);
            
            default:
                throw new IllegalArgumentException("Unknown algorithm type: " + type);
        }
    }
    
    /**
     * Get all supported algorithm types
     * 
     * @return Array of supported algorithm type names
     */
    public static String[] getSupportedAlgorithms() {
        return new String[]{"TOKEN_BUCKET", "SLIDING_WINDOW_LOG", "SLIDING_WINDOW_COUNTER"};
    }
    
    /**
     * Check if an algorithm type is supported
     * 
     * @param type The algorithm type to check
     * @return true if the algorithm type is supported
     */
    public static boolean isSupported(String type) {
        if (type == null) return false;
        String upperType = type.toUpperCase();
        return "TOKEN_BUCKET".equals(upperType) || 
               "SLIDING_WINDOW_LOG".equals(upperType) || 
               "SLIDING_WINDOW_COUNTER".equals(upperType);
    }
}