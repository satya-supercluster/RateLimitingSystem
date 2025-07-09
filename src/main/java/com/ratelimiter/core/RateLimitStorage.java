package com.ratelimiter.core;

import java.time.Duration;

/**
 * Storage Interface for Rate Limiter
 * Strategy Pattern - Different storage backends (Redis, In-Memory, etc.)
 */
public interface RateLimitStorage {
    /**
     * Increment the value for the given key
     * 
     * @param key The storage key
     * @param value The value to increment by
     * @param expiry The expiration time for the key
     */
    void increment(String key, long value, Duration expiry);
    
    /**
     * Get the value for the given key
     * 
     * @param key The storage key
     * @return The value, or 0 if key doesn't exist or is expired
     */
    long get(String key);
    
    /**
     * Set the value for the given key
     * 
     * @param key The storage key
     * @param value The value to set
     * @param expiry The expiration time for the key
     */
    void set(String key, long value, Duration expiry);
    
    /**
     * Delete the key from storage
     * 
     * @param key The storage key to delete
     */
    void delete(String key);
    
    /**
     * Check if the key exists in storage
     * 
     * @param key The storage key
     * @return true if the key exists and is not expired
     */
    boolean exists(String key);
}