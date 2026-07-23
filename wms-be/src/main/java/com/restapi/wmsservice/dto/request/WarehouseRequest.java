package com.restapi.wmsservice.dto.request;

import com.restapi.wmsservice.enums.WarehouseStatus;
import com.restapi.wmsservice.enums.WarehouseType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WarehouseRequest {

    @NotBlank(message = "WAREHOUSE_CODE_REQUIRED")
    String code;

    @NotBlank(message = "WAREHOUSE_NAME_REQUIRED")
    String name;

    @NotNull(message = "WAREHOUSE_TYPE_REQUIRED")
    WarehouseType type;

    @NotNull(message = "WAREHOUSE_STATUS_REQUIRED")
    WarehouseStatus status;
}
