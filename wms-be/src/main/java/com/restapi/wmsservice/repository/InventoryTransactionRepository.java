package com.restapi.wmsservice.repository;

import com.restapi.wmsservice.entity.InventoryTransaction;
import com.restapi.wmsservice.enums.TransactionStatus;
import com.restapi.wmsservice.enums.TransactionType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    @EntityGraph(attributePaths = {"details", "fromWarehouse", "toWarehouse", "details.item", "details.batchInfo", "details.locationFrom", "details.locationTo"})
    Optional<InventoryTransaction> findByTransactionNo(String transactionNo);

    @EntityGraph(attributePaths = {"details", "fromWarehouse", "toWarehouse", "details.item", "details.batchInfo", "details.locationFrom", "details.locationTo"})
    Optional<InventoryTransaction> findById(Long id);

    @EntityGraph(attributePaths = {"details", "fromWarehouse", "toWarehouse", "details.item", "details.batchInfo", "details.locationFrom", "details.locationTo"})
    List<InventoryTransaction> findAll();

    @EntityGraph(attributePaths = {"details", "fromWarehouse", "toWarehouse", "details.item", "details.batchInfo", "details.locationFrom", "details.locationTo"})
    Optional<InventoryTransaction> findFirstByTransactionTypeAndStatusAndReferenceTypeAndReferenceId(
            TransactionType transactionType,
            TransactionStatus status,
            String referenceType,
            Long referenceId);
}
