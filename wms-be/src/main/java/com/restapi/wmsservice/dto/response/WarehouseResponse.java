package com.restapi.wmsservice.dto.response;

import com.restapi.wmsservice.enums.WarehouseStatus;
import com.restapi.wmsservice.enums.WarehouseType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WarehouseResponse {
    Long id;
    String code;
    String name;
    WarehouseType type;
    WarehouseStatus status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
