package com.restapi.wmsservice.dto.request;

import com.restapi.wmsservice.enums.TransactionType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryTransactionRequest {

    @NotNull(message = "TRANSACTION_TYPE_REQUIRED")
    TransactionType transactionType;

    Long fromWarehouseId;

    Long toWarehouseId;

    String referenceType;

    Long referenceId;

    @NotEmpty(message = "TRANSACTION_DETAILS_REQUIRED")
    List<InventoryTransactionDetailRequest> details;
}
