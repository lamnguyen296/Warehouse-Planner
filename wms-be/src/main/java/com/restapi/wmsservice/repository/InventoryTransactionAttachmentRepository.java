package com.restapi.wmsservice.repository;

import com.restapi.wmsservice.entity.InventoryTransactionAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryTransactionAttachmentRepository
        extends JpaRepository<InventoryTransactionAttachment, Long> {

    List<InventoryTransactionAttachment> findAllByTransactionIdOrderByCreatedAtDesc(Long transactionId);

    Optional<InventoryTransactionAttachment> findByIdAndTransactionId(Long id, Long transactionId);
}
