package com.restapi.wmsservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryBatchStockResponse {
    Long id;
    Long inventoryId;
    Long batchId;
    String batchNo;
    Integer quantity;
}
