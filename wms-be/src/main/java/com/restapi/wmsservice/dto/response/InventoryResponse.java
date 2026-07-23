package com.restapi.wmsservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryResponse {
    Long id;
    Long warehouseId;
    String warehouseCode;
    Long locationId;
    String locationCode;
    Long itemId;
    String itemCode;
    Integer totalQuantity;
    Integer reservedQuantity;
    Integer availableQuantity;
    Long version;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
