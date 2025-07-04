package com.skishop.sales.service;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Order Number Generator
 * Utilizing Java 21's String Templates and modern features
 */
@Component
public class OrderNumberGenerator {

    private static final String PREFIX = "ORD";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final AtomicLong counter = new AtomicLong(1);

    /**
     * Generate order number
     * Format: ORD + YYYYMMDD + 6-digit sequence number
     * Example: ORD20240620000001
     * Using Java 21's String Template (preview feature)
     */
    public String generate() {
        var dateStr = LocalDateTime.now().format(DATE_FORMAT);
        var sequence = counter.getAndIncrement();
        var paddedSequence = String.format("%06d", sequence % 1000000);
        
        // Using Java 21's String.format() (String Template requires --enable-preview)
        return String.format("%s%s%s", PREFIX, dateStr, paddedSequence);
    }

    /**
     * Reset counter (for testing)
     */
    public void resetCounter() {
        counter.set(1);
    }

    /**
     * Get current counter value (for testing)
     */
    public long getCurrentCounter() {
        return counter.get();
    }
}
