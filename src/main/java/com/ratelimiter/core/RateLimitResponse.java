package com.ratelimiter.core;

import java.time.Duration;

/**
 * Rate Limit Response
 * Immutable response object for rate limit checks
 */
public class RateLimitResponse {
    private final boolean allowed;
    private final long remainingRequests;
    private final Duration retryAfter;
    private final long resetTime;
    
    private RateLimitResponse(boolean allowed, long remainingRequests, Duration retryAfter, long resetTime) {
        this.allowed = allowed;
        this.remainingRequests = remainingRequests;
        this.retryAfter = retryAfter;
        this.resetTime = resetTime;
    }
    
    /**
     * Create a response for an allowed request
     * 
     * @param remainingRequests Number of requests remaining in the current window
     * @param resetTime Timestamp when the rate limit will reset
     * @return RateLimitResponse indicating the request is allowed
     */
    public static RateLimitResponse allowed(long remainingRequests, long resetTime) {
        return new RateLimitResponse(true, remainingRequests, null, resetTime);
    }
    
    /**
     * Create a response for a denied request
     * 
     * @param retryAfter Duration to wait before retrying
     * @param resetTime Timestamp when the rate limit will reset
     * @return RateLimitResponse indicating the request is denied
     */
    public static RateLimitResponse denied(Duration retryAfter, long resetTime) {
        return new RateLimitResponse(false, 0, retryAfter, resetTime);
    }
    
    // Getters
    public boolean isAllowed() { 
        return allowed; 
    }
    
    public long getRemainingRequests() { 
        return remainingRequests; 
    }
    
    public Duration getRetryAfter() { 
        return retryAfter; 
    }
    
    public long getResetTime() { 
        return resetTime; 
    }
    
    @Override
    public String toString() {
        return String.format("RateLimitResponse{allowed=%s, remainingRequests=%d, retryAfter=%s, resetTime=%d}", 
                allowed, remainingRequests, retryAfter, resetTime);
    }
}