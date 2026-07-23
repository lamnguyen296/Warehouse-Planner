package com.restapi.wmsservice.dto.response;

import com.restapi.wmsservice.enums.PlanningAction;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PlanningDetailResponse {
    Long id;
    Long planningId;
    Long itemId;
    String itemCode;
    String itemName;
    Integer requiredQuantity;
    Integer availableQuantity;
    Integer recycleQuantity;
    Integer purchaseQuantity;
    Integer reservedQuantity;
    PlanningAction action;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
