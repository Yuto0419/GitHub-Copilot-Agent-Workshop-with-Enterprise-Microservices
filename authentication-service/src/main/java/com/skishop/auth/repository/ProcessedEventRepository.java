package com.skishop.auth.repository;

import com.skishop.auth.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository for processed events
 */
@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, String> {
    
    /**
     * Find by event type
     */
    List<ProcessedEvent> findByEventType(String eventType);
    
    /**
     * Find by success/failure
     */
    List<ProcessedEvent> findBySuccess(boolean success);
    
    /**
     * Query to find old processed events for deletion
     */
    @Query("SELECT p FROM ProcessedEvent p WHERE p.createdAt < :cutoffTime")
    List<ProcessedEvent> findOldProcessedEvents(@Param("cutoffTime") Instant cutoffTime);
    
    /**
     * Count processed events in a specific period
     */
    @Query("SELECT COUNT(p) FROM ProcessedEvent p WHERE p.createdAt >= :startTime AND p.createdAt <= :endTime AND p.success = :success")
    long countByPeriodAndSuccess(@Param("startTime") Instant startTime, 
                                @Param("endTime") Instant endTime, 
                                @Param("success") boolean success);
}
