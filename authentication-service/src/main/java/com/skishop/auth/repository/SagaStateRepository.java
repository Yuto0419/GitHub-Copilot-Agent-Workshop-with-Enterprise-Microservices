package com.skishop.auth.repository;

import com.skishop.auth.entity.SagaState;
import com.skishop.auth.enums.SagaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing Saga state
 */
@Repository
public interface SagaStateRepository extends JpaRepository<SagaState, Long> {
    
    /**
     * Find Saga by Saga ID
     */
    Optional<SagaState> findBySagaId(String sagaId);
    
    /**
     * Find Saga by entity ID
     */
    List<SagaState> findByEntityId(String entityId);
    
    /**
     * Find by user ID and event type
     */
    @Query("SELECT s FROM SagaState s WHERE s.userId = :userId AND s.eventType = :eventType")
    Optional<SagaState> findByUserIdAndEventType(@Param("userId") java.util.UUID userId, @Param("eventType") String eventType);
    
    /**
     * Find by Saga type and state
     */
    List<SagaState> findBySagaTypeAndState(String sagaType, String state);
    
    /**
     * Find old Sagas (for timeout processing)
     */
    @Query("SELECT s FROM SagaState s WHERE s.createdAt < :cutoffTime AND s.state NOT IN ('SAGA_COMPLETED', 'SAGA_FAILED', 'SAGA_TIMEOUT')")
    List<SagaState> findStaleStates(@Param("cutoffTime") Instant cutoffTime);
    
    /**
     * Count active Sagas
     */
    @Query("SELECT COUNT(s) FROM SagaState s WHERE s.state NOT IN ('SAGA_COMPLETED', 'SAGA_FAILED', 'SAGA_TIMEOUT')")
    long countActiveSagas();
    
    /**
     * Find Sagas by specific state
     */
    List<SagaState> findByState(String state);
    
    /**
     * Find timed-out Sagas (for compensation processing)
     */
    List<SagaState> findBySagaStatusAndTimeoutAtBefore(SagaStatus sagaStatus, Instant timeoutBefore);
    
    /**
     * Find Sagas that require compensation
     */
    @Query("SELECT s FROM SagaState s WHERE s.sagaStatus = :sagaStatus AND s.errorReason IS NOT NULL")
    List<SagaState> findBySagaStatusAndErrorReasonIsNotNull(@Param("sagaStatus") SagaStatus sagaStatus);
    
    /**
     * Find Sagas not in the specified statuses
     */
    List<SagaState> findBySagaStatusNotIn(List<SagaStatus> statuses);
    
    /**
     * Find Sagas started after the specified time
     */
    List<SagaState> findByStartTimeAfter(Instant startTime);
    
    /**
     * Find completed Sagas with end time
     */
    List<SagaState> findBySagaStatusAndEndTimeIsNotNull(SagaStatus sagaStatus);
    
    // Methods for monitoring functionality
    
    /**
     * Count by status
     */
    long countByStatus(String status);
    
    /**
     * Count by Saga status
     */
    long countBySagaStatus(SagaStatus sagaStatus);
    
    /**
     * Find by Saga type and start time
     */
    List<SagaState> findBySagaTypeAndStartTimeAfter(String sagaType, Instant startTime);
    
    /**
     * Find by Saga type (with paging)
     */
    org.springframework.data.domain.Page<SagaState> findBySagaType(String sagaType, org.springframework.data.domain.Pageable pageable);
    
    /**
     * Find by Saga status (with paging)
     */
    org.springframework.data.domain.Page<SagaState> findBySagaStatus(SagaStatus sagaStatus, org.springframework.data.domain.Pageable pageable);
    
    /**
     * Find by Saga type and Saga status (with paging)
     */
    org.springframework.data.domain.Page<SagaState> findBySagaTypeAndSagaStatus(String sagaType, SagaStatus sagaStatus, org.springframework.data.domain.Pageable pageable);
    
    /**
     * Find by multiple Saga statuses and last heartbeat before
     */
    List<SagaState> findBySagaStatusInAndLastHeartbeatBefore(List<SagaStatus> sagaStatuses, Instant lastHeartbeat);
    
    /**
     * Count by created at after and specific Saga status
     */
    long countByCreatedAtAfterAndSagaStatus(java.time.LocalDateTime createdAt, SagaStatus sagaStatus);
    
    /**
     * Get the number of Sagas completed after the specified time
     */
    @Query("SELECT COUNT(s) FROM SagaState s WHERE s.sagaStatus = 'SAGA_COMPLETED' AND s.endTime >= :since")
    long countCompletedAfter(@Param("since") Instant since);
    
    /**
     * Get the number of Sagas failed after the specified time
     */
    @Query("SELECT COUNT(s) FROM SagaState s WHERE s.sagaStatus = 'SAGA_FAILED' AND s.endTime >= :since")
    long countFailedAfter(@Param("since") Instant since);
    
    /**
     * Get average execution duration (milliseconds)
     */
    @Query("SELECT AVG(EXTRACT(EPOCH FROM (s.endTime - s.startTime)) * 1000) FROM SagaState s WHERE s.endTime IS NOT NULL AND s.startTime IS NOT NULL")
    Double getAverageExecutionDuration();
}
