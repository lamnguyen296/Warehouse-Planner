package com.restapi.wmsservice.repository;

import com.restapi.wmsservice.entity.InventoryReservation;
import com.restapi.wmsservice.enums.ReservationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Repository
public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {

    @EntityGraph(attributePaths = {"planningDetail", "item", "warehouse", "inventory", "inventory.location"})
    Optional<InventoryReservation> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM InventoryReservation r WHERE r.id = :id")
    Optional<InventoryReservation> findByIdForUpdate(@Param("id") Long id);

    @EntityGraph(attributePaths = {"item", "warehouse", "inventory", "inventory.location"})
    List<InventoryReservation> findByPlanningDetailId(Long planningDetailId);

    @EntityGraph(attributePaths = {"planningDetail", "item", "warehouse", "inventory", "inventory.location"})
    List<InventoryReservation> findAll();

    @EntityGraph(attributePaths = {"planningDetail", "item", "warehouse", "inventory", "inventory.location"})
    List<InventoryReservation> findByStatus(ReservationStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"planningDetail", "planningDetail.planning", "item", "warehouse", "inventory", "inventory.location"})
    @Query("SELECT r FROM InventoryReservation r " +
           "WHERE r.planningDetail.planning.id = :planningId " +
           "AND r.item.id = :itemId " +
           "AND r.warehouse.id = :warehouseId " +
           "AND r.status = :status ORDER BY r.id")
    List<InventoryReservation> findForConsumption(
            @Param("planningId") Long planningId,
            @Param("itemId") Long itemId,
            @Param("warehouseId") Long warehouseId,
            @Param("status") ReservationStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM InventoryReservation r WHERE r.status = :status " +
           "AND r.expiredTime IS NOT NULL AND r.expiredTime <= :now ORDER BY r.id")
    List<InventoryReservation> findExpiredForUpdate(
            @Param("status") ReservationStatus status,
            @Param("now") LocalDateTime now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM InventoryReservation r WHERE r.planningDetail.planning.id = :planningId " +
           "AND r.status = :status ORDER BY r.id")
    List<InventoryReservation> findByPlanningAndStatusForUpdate(
            @Param("planningId") Long planningId,
            @Param("status") ReservationStatus status);
}
