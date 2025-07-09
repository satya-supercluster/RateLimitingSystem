package com.ratelimiter.storage;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.ratelimiter.core.RateLimitStorage;

/**
 * In-Memory Storage Implementation
 * Thread-safe with TTL support and automatic cleanup
 */
public class InMemoryRateLimitStorage implements RateLimitStorage {
    private final ConcurrentHashMap<String, StorageEntry> storage;
    private final ScheduledExecutorService cleanupExecutor;
    
    private static class StorageEntry {
        final AtomicLong value;
        final long expiryTime;
        
        StorageEntry(long value, long expiryTime) {
            this.value = new AtomicLong(value);
            this.expiryTime = expiryTime;
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }
    
    public InMemoryRateLimitStorage() {
        this.storage = new ConcurrentHashMap<>();
        this.cleanupExecutor = Executors.newScheduledThreadPool(1);
        
        // Cleanup expired entries every 60 seconds
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredEntries, 60, 60, TimeUnit.SECONDS);
    }
    
    @Override
    public void increment(String key, long value, Duration expiry) {
        long expiryTime = System.currentTimeMillis() + expiry.toMillis();
        storage.compute(key, (k, entry) -> {
            if (entry == null || entry.isExpired()) {
                return new StorageEntry(value, expiryTime);
            }
            entry.value.addAndGet(value);
            return entry;
        });
    }
    
    @Override
    public long get(String key) {
        StorageEntry entry = storage.get(key);
        if (entry == null || entry.isExpired()) {
            return 0;
        }
        return entry.value.get();
    }
    
    @Override
    public void set(String key, long value, Duration expiry) {
        long expiryTime = System.currentTimeMillis() + expiry.toMillis();
        storage.put(key, new StorageEntry(value, expiryTime));
    }
    
    @Override
    public void delete(String key) {
        storage.remove(key);
    }
    
    @Override
    public boolean exists(String key) {
        StorageEntry entry = storage.get(key);
        return entry != null && !entry.isExpired();
    }
    
    /**
     * Clean up expired entries to prevent memory leaks
     */
    private void cleanupExpiredEntries() {
        storage.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    /**
     * Get the current size of the storage (for monitoring)
     * 
     * @return Number of entries in storage
     */
    public int size() {
        return storage.size();
    }
    
    /**
     * Clear all entries from storage
     */
    public void clear() {
        storage.clear();
    }
    
    /**
     * Shutdown the cleanup executor
     * Should be called when the storage is no longer needed
     */
    public void shutdown() {
        cleanupExecutor.shutdown();
        try {
            if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}