package com.ratelimiter.core;

/**
 * Rate Limiting Algorithm Interface
 * Strategy Pattern - Different algorithms can be plugged in
 */
public interface RateLimitingAlgorithm {
    /**
     * Check if a request is allowed for the given key at the specified timestamp
     * 
     * @param key The identifier for the rate limit (e.g., user ID, API key)
     * @param timestamp The timestamp of the request in milliseconds
     * @return true if the request is allowed, false otherwise
     */
    boolean isAllowed(String key, long timestamp);
    
    /**
     * Reset the rate limit state for the given key
     * 
     * @param key The identifier to reset
     */
    void reset(String key);
    
    /**
     * Get the name of the algorithm
     * 
     * @return The algorithm name
     */
    String getAlgorithmName();
}