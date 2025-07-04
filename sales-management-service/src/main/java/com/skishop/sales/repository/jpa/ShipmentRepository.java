package com.skishop.sales.repository.jpa;

import com.skishop.sales.entity.jpa.Shipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Shipment Repository
 */
@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {

    /**
     * Find shipment information by order ID
     */
    Optional<Shipment> findByOrderId(UUID orderId);

    /**
     * Find shipment information by tracking number
     */
    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    /**
     * Search by shipment status
     */
    Page<Shipment> findByStatus(Shipment.ShipmentStatus status, Pageable pageable);

    /**
     * Search by carrier
     */
    Page<Shipment> findByCarrier(String carrier, Pageable pageable);

    /**
     * Search by carrier and shipment status
     */
    Page<Shipment> findByCarrierAndStatus(String carrier, Shipment.ShipmentStatus status, Pageable pageable);

    /**
     * Search by shipping date period
     */
    @Query("SELECT s FROM Shipment s WHERE s.shippedAt BETWEEN :startDate AND :endDate")
    Page<Shipment> findByShippedAtBetween(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate, 
        Pageable pageable
    );

    /**
     * Search by estimated delivery date period
     */
    @Query("SELECT s FROM Shipment s WHERE s.estimatedDeliveryAt BETWEEN :startDate AND :endDate")
    Page<Shipment> findByEstimatedDeliveryAtBetween(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate, 
        Pageable pageable
    );

    /**
     * Search for shipments past their estimated delivery date
     */
    @Query("SELECT s FROM Shipment s WHERE s.estimatedDeliveryAt < :currentTime AND s.status NOT IN ('DELIVERED', 'RETURNED')")
    List<Shipment> findOverdueShipments(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Search by list of shipment statuses
     */
    @Query("SELECT s FROM Shipment s WHERE s.status IN :statuses")
    Page<Shipment> findByStatusIn(@Param("statuses") List<Shipment.ShipmentStatus> statuses, Pageable pageable);

    /**
     * Check if tracking number exists
     */
    boolean existsByTrackingNumber(String trackingNumber);

    /**
     * Count shipments by carrier
     */
    @Query("SELECT s.carrier, COUNT(s) FROM Shipment s GROUP BY s.carrier")
    List<Object[]> countShipmentsByCarrier();

    /**
     * Count shipments by status
     */
    @Query("SELECT s.status, COUNT(s) FROM Shipment s GROUP BY s.status")
    List<Object[]> countShipmentsByStatus();

    /**
     * Find shipment information by list of order IDs
     */
    @Query("SELECT s FROM Shipment s WHERE s.orderId IN :orderIds")
    List<Shipment> findByOrderIdIn(@Param("orderIds") List<UUID> orderIds);
}
