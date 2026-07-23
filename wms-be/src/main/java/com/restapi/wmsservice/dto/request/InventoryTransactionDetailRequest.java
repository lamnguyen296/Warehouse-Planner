package com.restapi.wmsservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryTransactionDetailRequest {

    @NotNull(message = "ITEM_ID_REQUIRED")
    Long itemId;

    Long batchId;

    Long locationFrom;

    Long locationTo;

    @NotNull(message = "QUANTITY_REQUIRED")
    Integer quantity;
}
