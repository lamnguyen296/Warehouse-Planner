package com.restapi.wmsservice.dto.response;

import com.restapi.wmsservice.enums.LocationStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LocationResponse {
    Long id;
    Long warehouseId;
    String warehouseCode;
    String code;
    String zone;
    String rack;
    Integer capacity;
    LocationStatus status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
