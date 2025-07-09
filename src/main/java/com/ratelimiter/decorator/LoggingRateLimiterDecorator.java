package com.ratelimiter.decorator;

import com.ratelimiter.core.RateLimitResponse;
import com.ratelimiter.service.RateLimiter;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Logging Decorator
 * Adds logging capabilities to rate limiter operations
 */
public class LoggingRateLimiterDecorator extends RateLimiterDecorator {
    private static final Logger logger = Logger.getLogger(LoggingRateLimiterDecorator.class.getName());
    
    private final boolean logAllRequests;
    private final boolean logOnlyDenied;
    
    /**
     * Constructor with default logging behavior (log all requests)
     * 
     * @param rateLimiter The rate limiter to decorate
     */
    public LoggingRateLimiterDecorator(RateLimiter rateLimiter) {
        this(rateLimiter, true, false);
    }
    
    /**
     * Constructor with configurable logging behavior
     * 
     * @param rateLimiter The rate limiter to decorate
     * @param logAllRequests Whether to log all requests
     * @param logOnlyDenied Whether to log only denied requests
     */
    public LoggingRateLimiterDecorator(RateLimiter rateLimiter, boolean logAllRequests, boolean logOnlyDenied) {
        super(rateLimiter);
        this.logAllRequests = logAllRequests;
        this.logOnlyDenied = logOnlyDenied;
    }
    
    @Override
    public RateLimitResponse checkLimit(String key) {
        long startTime = System.currentTimeMillis();
        
        try {
            RateLimitResponse response = rateLimiter.checkLimit(key);
            long endTime = System.currentTimeMillis();
            
            // Log based on configuration
            if (logAllRequests || (logOnlyDenied && !response.isAllowed())) {
                logRequest(key, response, endTime - startTime);
            }
            
            return response;
            
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            logError(key, e, endTime - startTime);
            throw e;
        }
    }
    
    /**
     * Log a rate limit request
     * 
     * @param key The request key
     * @param response The rate limit response
     * @param duration The duration of the request in milliseconds
     */
    private void logRequest(String key, RateLimitResponse response, long duration) {
        String status = response.isAllowed() ? "ALLOWED" : "DENIED";
        String message = String.format("[RATE_LIMITER] Key: %s, Status: %s, Duration: %dms, Remaining: %d", 
            key, status, duration, response.getRemainingRequests());
        
        if (response.isAllowed()) {
            logger.info(message);
        } else {
            logger.warning(message + String.format(", RetryAfter: %s", response.getRetryAfter()));
        }
    }
    
    /**
     * Log an error that occurred during rate limit checking
     * 
     * @param key The request key
     * @param exception The exception that occurred
     * @param duration The duration before the error occurred
     */
    private void logError(String key, Exception exception, long duration) {
        String message = String.format("[RATE_LIMITER_ERROR] Key: %s, Duration: %dms, Error: %s", 
            key, duration, exception.getMessage());
        logger.log(Level.SEVERE, message, exception);
    }
    
    /**
     * Enable debug logging for more detailed output
     */
    public void enableDebugLogging() {
        logger.setLevel(Level.FINE);
    }
}