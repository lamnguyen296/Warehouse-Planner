package com.restapi.wmsservice.dto.response;

import com.restapi.wmsservice.enums.ReservationStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryReservationResponse {
    Long id;
    Long planningDetailId;
    Long recycleOrderId;
    Long itemId;
    String itemCode;
    Long warehouseId;
    String warehouseCode;
    Integer quantity;
    ReservationStatus status;
    LocalDateTime expiredTime;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
