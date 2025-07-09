package com.ratelimiter.algorithms;

import java.time.Duration;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.ratelimiter.core.RateLimitStorage;
import com.ratelimiter.core.RateLimitingAlgorithm;

/**
 * Sliding Window Log Algorithm
 * Advanced: Maintains precise request timestamps for accurate rate limiting
 */
public class SlidingWindowLogAlgorithm implements RateLimitingAlgorithm {
    private final int maxRequests;
    private final Duration timeWindow;
    private final RateLimitStorage storage;
    private final ConcurrentHashMap<String, Queue<Long>> requestLogs;
    private final ReentrantReadWriteLock globalLock;
    
    public SlidingWindowLogAlgorithm(int maxRequests, Duration timeWindow, RateLimitStorage storage) {
        this.maxRequests = maxRequests;
        this.timeWindow = timeWindow;
        this.storage = storage;
        this.requestLogs = new ConcurrentHashMap<>();
        this.globalLock = new ReentrantReadWriteLock();
    }
    
    @Override
    public boolean isAllowed(String key, long timestamp) {
        globalLock.writeLock().lock();
        try {
            Queue<Long> requestLog = requestLogs.computeIfAbsent(key, k -> new LinkedList<>());
            
            // Remove old requests outside the time window
            long windowStart = timestamp - timeWindow.toMillis();
            requestLog.removeIf(requestTime -> requestTime < windowStart);
            
            if (requestLog.size() < maxRequests) {
                requestLog.offer(timestamp);
                return true;
            }
            
            return false;
        } finally {
            globalLock.writeLock().unlock();
        }
    }
    
    @Override
    public void reset(String key) {
        globalLock.writeLock().lock();
        try {
            requestLogs.remove(key);
        } finally {
            globalLock.writeLock().unlock();
        }
    }
    
    @Override
    public String getAlgorithmName() {
        return "SLIDING_WINDOW_LOG";
    }
    
    /**
     * Get the current request count for a key (for testing/monitoring)
     * 
     * @param key The key to check
     * @return Current request count in the window
     */
    public int getCurrentRequestCount(String key) {
        globalLock.readLock().lock();
        try {
            Queue<Long> requestLog = requestLogs.get(key);
            return requestLog != null ? requestLog.size() : 0;
        } finally {
            globalLock.readLock().unlock();
        }
    }
    
    /**
     * Clean up expired entries for memory management
     */
    public void cleanup() {
        globalLock.writeLock().lock();
        try {
            long currentTime = System.currentTimeMillis();
            long windowStart = currentTime - timeWindow.toMillis();
            
            requestLogs.entrySet().removeIf(entry -> {
                Queue<Long> requestLog = entry.getValue();
                requestLog.removeIf(requestTime -> requestTime < windowStart);
                return requestLog.isEmpty();
            });
        } finally {
            globalLock.writeLock().unlock();
        }
    }
}