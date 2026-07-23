package com.restapi.wmsservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransactionAttachmentResponse {
    private Long id;
    private Long transactionId;
    private String secureUrl;
    private String originalFilename;
    private String contentType;
    private Long fileSize;
    private String uploadedBy;
    private LocalDateTime createdAt;
}
