package com.ratelimiter.core;

import java.time.Duration;

/**
 * Rate Limit Rule Configuration
 * Immutable configuration object using Builder pattern
 */
public class RateLimitRule {
    private final int maxRequests;
    private final Duration timeWindow;
    private final String algorithmType;
    
    private RateLimitRule(Builder builder) {
        this.maxRequests = builder.maxRequests;
        this.timeWindow = builder.timeWindow;
        this.algorithmType = builder.algorithmType;
    }
    
    /**
     * Builder Pattern for flexible configuration
     */
    public static class Builder {
        private int maxRequests;
        private Duration timeWindow;
        private String algorithmType = "TOKEN_BUCKET";
        
        public Builder maxRequests(int maxRequests) {
            this.maxRequests = maxRequests;
            return this;
        }
        
        public Builder timeWindow(Duration timeWindow) {
            this.timeWindow = timeWindow;
            return this;
        }
        
        public Builder algorithm(String algorithmType) {
            this.algorithmType = algorithmType;
            return this;
        }
        
        public RateLimitRule build() {
            if (maxRequests <= 0) {
                throw new IllegalArgumentException("Max requests must be positive");
            }
            if (timeWindow == null || timeWindow.isZero()) {
                throw new IllegalArgumentException("Time window must be valid");
            }
            return new RateLimitRule(this);
        }
    }
    
    // Getters
    public int getMaxRequests() { 
        return maxRequests; 
    }
    
    public Duration getTimeWindow() { 
        return timeWindow; 
    }
    
    public String getAlgorithmType() { 
        return algorithmType; 
    }
    
    @Override
    public String toString() {
        return String.format("RateLimitRule{maxRequests=%d, timeWindow=%s, algorithmType='%s'}", 
                maxRequests, timeWindow, algorithmType);
    }
}