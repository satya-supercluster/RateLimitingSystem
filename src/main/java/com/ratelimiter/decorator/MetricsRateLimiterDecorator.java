package com.ratelimiter.decorator;

import com.ratelimiter.core.RateLimitResponse;
import com.ratelimiter.service.RateLimiter;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Metrics Decorator
 * Adds metrics collection to rate limiter operations
 */
public class MetricsRateLimiterDecorator extends RateLimiterDecorator {
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong allowedRequests = new AtomicLong(0);
    private final AtomicLong deniedRequests = new AtomicLong(0);
    private final LongAdder totalResponseTime = new LongAdder();
    private final AtomicLong maxResponseTime = new AtomicLong(0);
    private final AtomicLong minResponseTime = new AtomicLong(Long.MAX_VALUE);
    
    // Per-key metrics
    private final ConcurrentHashMap<String, KeyMetrics> keyMetrics = new ConcurrentHashMap<>();
    
    /**
     * Metrics for individual keys
     */
    private static class KeyMetrics {
        final AtomicLong requests = new AtomicLong(0);
        final AtomicLong allowed = new AtomicLong(0);
        final AtomicLong denied = new AtomicLong(0);
        
        double getSuccessRate() {
            long total = requests.get();
            return total > 0 ? (allowed.get() * 100.0 / total) : 0.0;
        }
    }
    
    public MetricsRateLimiterDecorator(RateLimiter rateLimiter) {
        super(rateLimiter);
    }
    
    @Override
    public RateLimitResponse checkLimit(String key) {
        long startTime = System.currentTimeMillis();
        totalRequests.incrementAndGet();
        
        // Update per-key metrics
        KeyMetrics keyMetric = keyMetrics.computeIfAbsent(key, k -> new KeyMetrics());
        keyMetric.requests.incrementAndGet();
        
        try {
            RateLimitResponse response = rateLimiter.checkLimit(key);
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            // Update global metrics
            updateResponseTimeMetrics(duration);
            
            if (response.isAllowed()) {
                allowedRequests.incrementAndGet();
                keyMetric.allowed.incrementAndGet();
            } else {
                deniedRequests.incrementAndGet();
                keyMetric.denied.incrementAndGet();
            }
            
            return response;
            
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            updateResponseTimeMetrics(endTime - startTime);
            throw e;
        }
    }
    
    /**
     * Update response time metrics
     * 
     * @param duration The response time in milliseconds
     */
    private void updateResponseTimeMetrics(long duration) {
        totalResponseTime.add(duration);
        
        // Update max response time
        long currentMax = maxResponseTime.get();
        while (duration > currentMax) {
            if (maxResponseTime.compareAndSet(currentMax, duration)) {
                break;
            }
            currentMax = maxResponseTime.get();
        }
        
        // Update min response time
        long currentMin = minResponseTime.get();
        while (duration < currentMin) {
            if (minResponseTime.compareAndSet(currentMin, duration)) {
                break;
            }
            currentMin = minResponseTime.get();
        }
    }
    
    /**
     * Print comprehensive metrics to console
     */
    public void printMetrics() {
        System.out.println("=== RATE LIMITER METRICS ===");
        System.out.println("Total Requests: " + totalRequests.get());
        System.out.println("Allowed Requests: " + allowedRequests.get());
        System.out.println("Denied Requests: " + deniedRequests.get());
        System.out.printf("Success Rate: %.2f%%%n", getSuccessRate());
        System.out.printf("Average Response Time: %.2f ms%n", getAverageResponseTime());
        System.out.println("Max Response Time: " + maxResponseTime.get() + " ms");
        System.out.println("Min Response Time: " + (minResponseTime.get() == Long.MAX_VALUE ? 0 : minResponseTime.get()) + " ms");
        
        if (!keyMetrics.isEmpty()) {
            System.out.println("\n=== PER-KEY METRICS ===");
            for (Map.Entry<String, KeyMetrics> entry : keyMetrics.entrySet()) {
                String key = entry.getKey();
                KeyMetrics metrics = entry.getValue();
                System.out.printf("Key: %s - Requests: %d, Allowed: %d, Denied: %d, Success Rate: %.2f%%%n",
                    key, metrics.requests.get(), metrics.allowed.get(), 
                    metrics.denied.get(), metrics.getSuccessRate());
            }
        }
    }
    
    /**
     * Get the overall success rate
     * 
     * @return Success rate as a percentage
     */
    public double getSuccessRate() {
        long total = totalRequests.get();
        return total > 0 ? (allowedRequests.get() * 100.0 / total) : 0.0;
    }
    
    /**
     * Get the average response time
     * 
     * @return Average response time in milliseconds
     */
    public double getAverageResponseTime() {
        long total = totalRequests.get();
        return total > 0 ? (totalResponseTime.sum() / (double) total) : 0.0;
    }
    
    /**
     * Get metrics for a specific key
     * 
     * @param key The key to get metrics for
     * @return KeyMetrics for the specified key, or null if not found
     */
    public KeyMetrics getKeyMetrics(String key) {
        return keyMetrics.get(key);
    }
    
    /**
     * Reset all metrics
     */
    public void resetMetrics() {
        totalRequests.set(0);
        allowedRequests.set(0);
        deniedRequests.set(0);
        totalResponseTime.reset();
        maxResponseTime.set(0);
        minResponseTime.set(Long.MAX_VALUE);
        keyMetrics.clear();
    }
    
    /**
     * Get total number of requests
     * 
     * @return Total requests count
     */
    public long getTotalRequests() {
        return totalRequests.get();
    }
    
    /**
     * Get number of allowed requests
     * 
     * @return Allowed requests count
     */
    public long getAllowedRequests() {
        return allowedRequests.get();
    }
    
    /**
     * Get number of denied requests
     * 
     * @return Denied requests count
     */
    public long getDeniedRequests() {
        return deniedRequests.get();
    }
}