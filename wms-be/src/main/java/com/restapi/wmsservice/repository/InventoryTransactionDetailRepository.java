package com.restapi.wmsservice.repository;

import com.restapi.wmsservice.entity.InventoryTransactionDetail;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryTransactionDetailRepository extends JpaRepository<InventoryTransactionDetail, Long> {

    @EntityGraph(attributePaths = {"item", "batchInfo", "locationFrom", "locationTo"})
    List<InventoryTransactionDetail> findByTransactionId(Long transactionId);
}
