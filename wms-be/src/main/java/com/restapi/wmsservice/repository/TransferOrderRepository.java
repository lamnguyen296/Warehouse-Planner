package com.restapi.wmsservice.repository;

import com.restapi.wmsservice.entity.TransferOrder;
import com.restapi.wmsservice.enums.TransferStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransferOrderRepository extends JpaRepository<TransferOrder, Long> {

    @EntityGraph(attributePaths = {"planning", "fromWarehouse", "toWarehouse", "item"})
    Optional<TransferOrder> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TransferOrder t WHERE t.id = :id")
    Optional<TransferOrder> findByIdForUpdate(@Param("id") Long id);

    @EntityGraph(attributePaths = {"planning", "fromWarehouse", "toWarehouse", "item"})
    List<TransferOrder> findAll();

    boolean existsByTransferNo(String transferNo);

    @EntityGraph(attributePaths = {"planning", "fromWarehouse", "toWarehouse", "item"})
    List<TransferOrder> findByStatus(TransferStatus status);

    @EntityGraph(attributePaths = {"planning", "fromWarehouse", "toWarehouse", "item"})
    List<TransferOrder> findByItemId(Long itemId);

    @EntityGraph(attributePaths = {"planning", "fromWarehouse", "toWarehouse", "item"})
    List<TransferOrder> findByPlanningId(Long planningId);
}
