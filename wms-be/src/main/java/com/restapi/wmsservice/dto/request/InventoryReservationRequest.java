package com.restapi.wmsservice.dto.request;

import com.restapi.wmsservice.enums.ReservationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryReservationRequest {

    @NotNull(message = "PLANNING_DETAIL_ID_REQUIRED")
    Long planningDetailId;

    @NotNull(message = "ITEM_ID_REQUIRED")
    Long itemId;

    @NotNull(message = "WAREHOUSE_ID_REQUIRED")
    Long warehouseId;

    @NotNull(message = "QUANTITY_REQUIRED")
    @Min(value = 1, message = "QUANTITY_MIN")
    Integer quantity;

    @NotNull(message = "RESERVATION_STATUS_REQUIRED")
    ReservationStatus status;

    LocalDateTime expiredTime;
}
