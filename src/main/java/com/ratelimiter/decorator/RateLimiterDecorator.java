package com.ratelimiter.decorator;

import com.ratelimiter.core.RateLimitResponse;
import com.ratelimiter.service.RateLimiter;

/**
 * Rate Limiter Decorator Base
 * Allows adding cross-cutting concerns using the Decorator pattern
 */
public abstract class RateLimiterDecorator {
    protected final RateLimiter rateLimiter;
    
    /**
     * Constructor for the decorator
     * 
     * @param rateLimiter The rate limiter instance to decorate
     */
    public RateLimiterDecorator(RateLimiter rateLimiter) {
        if (rateLimiter == null) {
            throw new IllegalArgumentException("Rate limiter cannot be null");
        }
        this.rateLimiter = rateLimiter;
    }
    
    /**
     * Check rate limit for the given key
     * This method should be implemented by concrete decorators
     * 
     * @param key The key to check rate limit for
     * @return RateLimitResponse indicating whether the request is allowed
     */
    public abstract RateLimitResponse checkLimit(String key);
    
    /**
     * Get the underlying rate limiter instance
     * 
     * @return The decorated rate limiter
     */
    protected RateLimiter getUnderlying() {
        return rateLimiter;
    }
}