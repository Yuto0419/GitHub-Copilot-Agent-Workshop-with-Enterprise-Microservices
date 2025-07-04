package com.skishop.auth.repository;

import com.skishop.auth.entity.SagaStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Saga status history
 */
@Repository
public interface SagaStatusHistoryRepository extends JpaRepository<SagaStatusHistory, Long> {
    
    /**
     * Get history for the specified Saga ID in chronological order
     */
    List<SagaStatusHistory> findBySagaIdOrderByTransitionTimeAsc(String sagaId);
    
    /**
     * Get status transition history within the specified period
     */
    List<SagaStatusHistory> findByTransitionTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Get status transitions where an error occurred
     */
    List<SagaStatusHistory> findByErrorMessageIsNotNullOrderByTransitionTimeDesc();
    
    /**
     * Get transition history to the specified status
     */
    List<SagaStatusHistory> findByToStatusOrderByTransitionTimeDesc(String toStatus);
    
    /**
     * Get Saga status transition statistics
     */
    @Query("SELECT h.toStatus, COUNT(h) FROM SagaStatusHistory h WHERE h.transitionTime >= :since GROUP BY h.toStatus")
    List<Object[]> getStatusTransitionStats(@Param("since") LocalDateTime since);
}
