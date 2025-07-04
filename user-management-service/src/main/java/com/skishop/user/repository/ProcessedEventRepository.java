package com.skishop.user.repository;

import com.skishop.user.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {
    
    /**
     * Check existence of processed event by Saga ID
     */
    boolean existsBySagaId(String sagaId);
    
    /**
     * Delete old processed events (for cleanup)
     */
    @Modifying
    @Query("DELETE FROM ProcessedEvent p WHERE p.processedAt < :cutoffDate")
    int deleteOldProcessedEvents(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Count processed events within a specific period
     */
    @Query("SELECT COUNT(p) FROM ProcessedEvent p WHERE p.processedAt >= :fromDate AND p.processedAt < :toDate")
    long countProcessedEventsBetween(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);

    /**
     * Check existence of processed event by event ID
     */
    boolean existsByEventId(String eventId);

    /**
     * Find processed event by event ID
     */
    ProcessedEvent findByEventId(String eventId);
}
