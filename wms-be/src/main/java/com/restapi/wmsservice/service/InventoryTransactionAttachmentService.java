package com.restapi.wmsservice.service;

import com.restapi.wmsservice.dto.response.InventoryTransactionAttachmentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface InventoryTransactionAttachmentService {

    List<InventoryTransactionAttachmentResponse> getAll(Long transactionId);

    InventoryTransactionAttachmentResponse upload(Long transactionId, MultipartFile file);

    void delete(Long transactionId, Long attachmentId);
}
