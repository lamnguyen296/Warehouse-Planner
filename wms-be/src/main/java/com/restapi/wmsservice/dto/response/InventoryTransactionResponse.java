package com.restapi.wmsservice.dto.response;

import com.restapi.wmsservice.enums.TransactionStatus;
import com.restapi.wmsservice.enums.TransactionType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryTransactionResponse {
    Long id;
    String transactionNo;
    TransactionType transactionType;
    TransactionStatus status;
    Long fromWarehouseId;
    String fromWarehouseCode;
    Long toWarehouseId;
    String toWarehouseCode;
    String referenceType;
    Long referenceId;
    String createdBy;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    List<InventoryTransactionDetailResponse> details;
}
