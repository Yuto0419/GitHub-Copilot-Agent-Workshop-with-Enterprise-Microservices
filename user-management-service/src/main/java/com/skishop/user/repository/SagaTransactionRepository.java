package com.skishop.user.repository;

import com.skishop.user.entity.SagaTransaction;
import com.skishop.user.enums.SagaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SagaTransaction repository
 * Saga pattern state management and query operations
 */
@Repository
public interface SagaTransactionRepository extends JpaRepository<SagaTransaction, String> {

    /**
     * Find Saga transaction by correlation ID
     */
    Optional<SagaTransaction> findByCorrelationId(String correlationId);

    /**
     * Find Saga transaction by original event ID
     */
    Optional<SagaTransaction> findByOriginalEventId(String originalEventId);

    /**
     * Find Saga transactions by user ID
     */
    List<SagaTransaction> findByUserId(String userId);

    /**
     * Find Saga transactions by status
     */
    List<SagaTransaction> findByStatus(SagaStatus status);

    /**
     * Find Saga transactions by specific status list
     */
    List<SagaTransaction> findByStatusIn(List<SagaStatus> statuses);

    /**
     * Find timed-out Saga transactions
     */
    @Query("SELECT s FROM SagaTransaction s WHERE s.timeoutAt < :currentTime AND s.status NOT IN :terminalStatuses")
    List<SagaTransaction> findTimedOutSagas(
        @Param("currentTime") LocalDateTime currentTime,
        @Param("terminalStatuses") List<SagaStatus> terminalStatuses
    );

    /**
     * Find retryable Saga transactions
     */
    @Query("SELECT s FROM SagaTransaction s WHERE s.status = :status AND s.retryCount < s.maxRetryCount AND (s.timeoutAt IS NULL OR s.timeoutAt > :currentTime)")
    List<SagaTransaction> findRetryableSagas(
        @Param("status") SagaStatus status,
        @Param("currentTime") LocalDateTime currentTime
    );

    /**
     * Find Saga transactions within specific time period
     */
    @Query("SELECT s FROM SagaTransaction s WHERE s.createdAt BETWEEN :startTime AND :endTime")
    List<SagaTransaction> findByCreatedAtBetween(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * Get statistics by event type
     */
    @Query("SELECT s.eventType, s.status, COUNT(s) FROM SagaTransaction s GROUP BY s.eventType, s.status")
    List<Object[]> findStatisticsByEventTypeAndStatus();

    /**
     * Count running Sagas
     */
    @Query("SELECT COUNT(s) FROM SagaTransaction s WHERE s.status IN :activeStatuses")
    Long countActiveSagas(@Param("activeStatuses") List<SagaStatus> activeStatuses);

    /**
     * Calculate average processing time
     */
    @Query("SELECT AVG(EXTRACT(EPOCH FROM (s.processingEndTime - s.processingStartTime)) * 1000) FROM SagaTransaction s WHERE s.processingStartTime IS NOT NULL AND s.processingEndTime IS NOT NULL AND s.eventType = :eventType")
    Double findAverageProcessingTimeByEventType(@Param("eventType") String eventType);

    /**
     * Find the latest Saga by user ID and event type
     */
    Optional<SagaTransaction> findTopByUserIdAndEventTypeOrderByCreatedAtDesc(String userId, String eventType);

    /**
     * Find old Saga transactions for cleanup
     */
    @Query("SELECT s FROM SagaTransaction s WHERE s.createdAt < :cutoffTime AND s.status IN :terminalStatuses")
    List<SagaTransaction> findOldCompletedSagas(
        @Param("cutoffTime") LocalDateTime cutoffTime,
        @Param("terminalStatuses") List<SagaStatus> terminalStatuses
    );

    /**
     * Find details of failed Sagas
     */
    @Query("SELECT s FROM SagaTransaction s WHERE s.status IN :failureStatuses AND s.createdAt >= :since ORDER BY s.createdAt DESC")
    List<SagaTransaction> findRecentFailedSagas(
        @Param("failureStatuses") List<SagaStatus> failureStatuses,
        @Param("since") LocalDateTime since
    );
}
