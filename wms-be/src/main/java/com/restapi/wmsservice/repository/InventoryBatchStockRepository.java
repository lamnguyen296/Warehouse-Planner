package com.restapi.wmsservice.repository;

import com.restapi.wmsservice.entity.InventoryBatchStock;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryBatchStockRepository extends JpaRepository<InventoryBatchStock, Long> {

    @EntityGraph(attributePaths = {"inventory", "batchInfo"})
    Optional<InventoryBatchStock> findByInventoryIdAndBatchInfoId(Long inventoryId, Long batchInfoId);
}
