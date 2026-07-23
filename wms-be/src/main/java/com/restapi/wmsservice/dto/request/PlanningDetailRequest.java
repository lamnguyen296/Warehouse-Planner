package com.restapi.wmsservice.dto.request;

import com.restapi.wmsservice.enums.PlanningAction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlanningDetailRequest {

    @NotNull(message = "ITEM_ID_REQUIRED")
    Long itemId;

    @NotNull(message = "REQUIRED_QUANTITY_REQUIRED")
    @Min(value = 1, message = "REQUIRED_QUANTITY_MIN")
    Integer requiredQuantity;

    @Min(value = 0, message = "AVAILABLE_QUANTITY_MIN")
    Integer availableQuantity;
    @Min(value = 0, message = "RECYCLE_QUANTITY_MIN")
    Integer recycleQuantity;
    @Min(value = 0, message = "PURCHASE_QUANTITY_MIN")
    Integer purchaseQuantity;
    @Min(value = 0, message = "RESERVED_QUANTITY_MIN")
    Integer reservedQuantity;
    PlanningAction action;
}
