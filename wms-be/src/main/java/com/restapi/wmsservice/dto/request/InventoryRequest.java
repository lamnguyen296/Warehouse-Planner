package com.restapi.wmsservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryRequest {

    @NotNull(message = "WAREHOUSE_ID_REQUIRED")
    Long warehouseId;

    @NotNull(message = "LOCATION_ID_REQUIRED")
    Long locationId;

    @NotNull(message = "ITEM_ID_REQUIRED")
    Long itemId;

    @NotNull(message = "TOTAL_QUANTITY_REQUIRED")
    @Min(value = 0, message = "TOTAL_QUANTITY_MIN")
    Integer totalQuantity;

    @NotNull(message = "RESERVED_QUANTITY_REQUIRED")
    @Min(value = 0, message = "RESERVED_QUANTITY_MIN")
    Integer reservedQuantity;

    @NotNull(message = "AVAILABLE_QUANTITY_REQUIRED")
    @Min(value = 0, message = "AVAILABLE_QUANTITY_MIN")
    Integer availableQuantity;
}
