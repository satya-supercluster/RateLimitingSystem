package com.ratelimiter.demo;

import java.time.Duration;

import com.ratelimiter.core.RateLimitResponse;
import com.ratelimiter.core.RateLimitRule;
import com.ratelimiter.decorator.LoggingRateLimiterDecorator;
import com.ratelimiter.decorator.MetricsRateLimiterDecorator;
import com.ratelimiter.decorator.RateLimiterDecorator;
import com.ratelimiter.factory.StorageFactory;
import com.ratelimiter.service.RateLimiter;

public class RateLimiterDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== ADVANCED RATE LIMITER SYSTEM DEMO ===\n");

        RateLimiter rateLimiter = new RateLimiter.Builder()
            .addRule("user:123", new RateLimitRule.Builder()
                .maxRequests(5)
                .timeWindow(Duration.ofSeconds(10))
                .algorithm("TOKEN_BUCKET")
                .build())
            .addRule("api:login", new RateLimitRule.Builder()
                .maxRequests(3)
                .timeWindow(Duration.ofSeconds(20))
                .algorithm("SLIDING_WINDOW_LOG")
                .build())
            .addRule("api:search", new RateLimitRule.Builder()
                .maxRequests(100)
                .timeWindow(Duration.ofMinutes(1))
                .algorithm("SLIDING_WINDOW_COUNTER")
                .build())
            .storage(StorageFactory.createStorage("MEMORY"))
            .build();

        LoggingRateLimiterDecorator loggingDecorator = new LoggingRateLimiterDecorator(rateLimiter);
        MetricsRateLimiterDecorator metricsDecorator = new MetricsRateLimiterDecorator(rateLimiter);

        System.out.println("1. Testing Token Bucket Algorithm (user:123):");
        testRateLimit(loggingDecorator, "user:123", 20, 500); // 20 rapid requests with 500ms interval

        System.out.println("\n2. Testing Sliding Window Log Algorithm (api:login):");
        testRateLimit(loggingDecorator, "api:login", 10, 1000); // slower, but exceeds limit

        System.out.println("\n3. Testing Sliding Window Counter Algorithm (api:search):");
        testRateLimit(metricsDecorator, "api:search", 120, 200); // rapid fire 120 requests

        System.out.println("\n4. Testing Token Bucket Recovery (user:123):");
        System.out.println("Waiting 11 seconds for token bucket to refill...");
        Thread.sleep(11000);
        testRateLimit(loggingDecorator, "user:123", 5, 500); // retesting after recovery

        System.out.println();
        metricsDecorator.printMetrics();

        System.out.println("\n=== DEMO COMPLETED ===");
    }

    private static void testRateLimit(RateLimiterDecorator decorator, String key, int requests, int intervalMillis) throws InterruptedException {
        for (int i = 1; i <= requests; i++) {
            RateLimitResponse response = decorator.checkLimit(key);
            System.out.printf("Request %d: %s", i, response.isAllowed() ? "ALLOWED" : "DENIED");
            if (!response.isAllowed()) {
                System.out.printf(" (Retry after: %d seconds)", response.getRetryAfter().getSeconds());
            }
            System.out.println();
            Thread.sleep(intervalMillis);
        }
    }
}
