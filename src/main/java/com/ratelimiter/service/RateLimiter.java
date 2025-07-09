package com.ratelimiter.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ratelimiter.core.RateLimitResponse;
import com.ratelimiter.core.RateLimitRule;
import com.ratelimiter.core.RateLimitStorage;
import com.ratelimiter.core.RateLimitingAlgorithm;
import com.ratelimiter.factory.RateLimitingAlgorithmFactory;
import com.ratelimiter.storage.InMemoryRateLimitStorage;

/**
 * Main Rate Limiter Implementation
 * Orchestrates all components together using various design patterns
 */
public class RateLimiter {
    private final Map<String, RateLimitingAlgorithm> algorithms;
    private final Map<String, RateLimitRule> rules;
    private final RateLimitStorage storage;
    
    /**
     * Private constructor for builder pattern
     */
    private RateLimiter(Builder builder) {
        this.algorithms = new ConcurrentHashMap<>();
        this.rules = new ConcurrentHashMap<>(builder.rules);
        this.storage = builder.storage;
        
        // Initialize algorithms based on rules
        initializeAlgorithms();
    }
    
    /**
     * Initialize algorithms for all configured rules
     */
    private void initializeAlgorithms() {
        for (Map.Entry<String, RateLimitRule> entry : rules.entrySet()) {
            String key = entry.getKey();
            RateLimitRule rule = entry.getValue();
            RateLimitingAlgorithm algorithm = RateLimitingAlgorithmFactory.createAlgorithm(
                rule.getAlgorithmType(), rule, storage);
            algorithms.put(key, algorithm);
        }
    }
    
    /**
     * Check rate limit for the given key
     * 
     * @param key The key to check rate limit for
     * @return RateLimitResponse indicating whether the request is allowed
     */
    public RateLimitResponse checkLimit(String key) {
        RateLimitRule rule = rules.get(key);
        if (rule == null) {
            // Default: allow if no rule exists
            return RateLimitResponse.allowed(Long.MAX_VALUE, System.currentTimeMillis());
        }
        
        RateLimitingAlgorithm algorithm = algorithms.get(key);
        if (algorithm == null) {
            // This shouldn't happen if properly initialized, but handle gracefully
            algorithm = RateLimitingAlgorithmFactory.createAlgorithm(
                rule.getAlgorithmType(), rule, storage);
            algorithms.put(key, algorithm);
        }
        
        long timestamp = System.currentTimeMillis();
        
        if (algorithm.isAllowed(key, timestamp)) {
            long resetTime = timestamp + rule.getTimeWindow().toMillis();
            // Calculate remaining requests (approximation)
            long remaining = Math.max(0, rule.getMaxRequests() - 1);
            return RateLimitResponse.allowed(remaining, resetTime);
        } else {
            long resetTime = timestamp + rule.getTimeWindow().toMillis();
            return RateLimitResponse.denied(rule.getTimeWindow(), resetTime);
        }
    }
    
    /**
     * Add a new rate limit rule
     * 
     * @param key The key to apply the rule to
     * @param rule The rate limit rule
     */
    public void addRule(String key, RateLimitRule rule) {
        if (key == null || rule == null) {
            throw new IllegalArgumentException("Key and rule cannot be null");
        }
        
        rules.put(key, rule);
        RateLimitingAlgorithm algorithm = RateLimitingAlgorithmFactory.createAlgorithm(
            rule.getAlgorithmType(), rule, storage);
        algorithms.put(key, algorithm);
    }
    
    /**
     * Remove a rate limit rule
     * 
     * @param key The key to remove the rule for
     */
    public void removeRule(String key) {
        if (key == null) {
            return;
        }
        
        rules.remove(key);
        RateLimitingAlgorithm algorithm = algorithms.remove(key);
        if (algorithm != null) {
            algorithm.reset(key);
        }
    }
    
    /**
     * Get the rule for a specific key
     * 
     * @param key The key to get the rule for
     * @return The rate limit rule, or null if not found
     */
    public RateLimitRule getRule(String key) {
        return rules.get(key);
    }
    
    /**
     * Get all configured rule keys
     * 
     * @return Set of all rule keys
     */
    public java.util.Set<String> getRuleKeys() {
        return new java.util.HashSet<>(rules.keySet());
    }
    
    /**
     * Update an existing rule
     * 
     * @param key The key to update
     * @param newRule The new rule
     */
    public void updateRule(String key, RateLimitRule newRule) {
        if (key == null || newRule == null) {
            throw new IllegalArgumentException("Key and rule cannot be null");
        }
        
        // Remove old rule and algorithm
        removeRule(key);
        // Add new rule
        addRule(key, newRule);
    }
    
    /**
     * Reset rate limit state for a specific key
     * 
     * @param key The key to reset
     */
    public void resetKey(String key) {
        RateLimitingAlgorithm algorithm = algorithms.get(key);
        if (algorithm != null) {
            algorithm.reset(key);
        }
    }
    
    /**
     * Get the storage instance used by this rate limiter
     * 
     * @return The storage instance
     */
    public RateLimitStorage getStorage() {
        return storage;
    }
    
    /**
     * Builder Pattern for Rate Limiter
     * Provides a fluent interface for constructing rate limiter instances
     */
    public static class Builder {
        private final Map<String, RateLimitRule> rules = new HashMap<>();
        private RateLimitStorage storage = new InMemoryRateLimitStorage();
        
        /**
         * Add a rate limit rule
         * 
         * @param key The key to apply the rule to
         * @param rule The rate limit rule
         * @return This builder instance
         */
        public Builder addRule(String key, RateLimitRule rule) {
            if (key == null || rule == null) {
                throw new IllegalArgumentException("Key and rule cannot be null");
            }
            rules.put(key, rule);
            return this;
        }
        
        /**
         * Set the storage implementation
         * 
         * @param storage The storage implementation
         * @return This builder instance
         */
        public Builder storage(RateLimitStorage storage) {
            if (storage == null) {
                throw new IllegalArgumentException("Storage cannot be null");
            }
            this.storage = storage;
            return this;
        }
        
        /**
         * Build the rate limiter instance
         * 
         * @return The configured rate limiter
         */
        public RateLimiter build() {
            return new RateLimiter(this);
        }
    }
}