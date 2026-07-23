package com.restapi.wmsservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkshopRequestDetailResponse {
    Long id;
    Long workshopRequestId;
    Long itemId;
    String itemCode;
    String itemName;
    Integer quantity;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
