package com.ratelimiter.factory;

import com.ratelimiter.core.RateLimitStorage;
import com.ratelimiter.storage.InMemoryRateLimitStorage;

/**
 * Storage Factory
 * Factory Pattern for creating storage implementations
 */
public class StorageFactory {
    
    /**
     * Create a storage implementation based on the specified type
     * 
     * @param type The storage type (MEMORY, REDIS, etc.)
     * @return The created storage instance
     * @throws IllegalArgumentException if the storage type is unknown
     * @throws UnsupportedOperationException if the storage type is not implemented
     */
    public static RateLimitStorage createStorage(String type) {
        switch (type.toUpperCase()) {
            case "MEMORY":
                return new InMemoryRateLimitStorage();
            
            case "REDIS":
                // In production, this would create a Redis-backed storage
                throw new UnsupportedOperationException("Redis storage not implemented in this demo");
            
            case "DATABASE":
                // In production, this would create a database-backed storage
                throw new UnsupportedOperationException("Database storage not implemented in this demo");
            
            default:
                throw new IllegalArgumentException("Unknown storage type: " + type);
        }
    }
    
    /**
     * Get all supported storage types
     * 
     * @return Array of supported storage type names
     */
    public static String[] getSupportedStorageTypes() {
        return new String[]{"MEMORY", "REDIS", "DATABASE"};
    }
    
    /**
     * Check if a storage type is supported
     * 
     * @param type The storage type to check
     * @return true if the storage type is supported
     */
    public static boolean isSupported(String type) {
        if (type == null) return false;
        String upperType = type.toUpperCase();
        return "MEMORY".equals(upperType) || 
               "REDIS".equals(upperType) || 
               "DATABASE".equals(upperType);
    }
    
    /**
     * Check if a storage type is implemented
     * 
     * @param type The storage type to check
     * @return true if the storage type is implemented
     */
    public static boolean isImplemented(String type) {
        if (type == null) return false;
        return "MEMORY".equals(type.toUpperCase());
    }
}