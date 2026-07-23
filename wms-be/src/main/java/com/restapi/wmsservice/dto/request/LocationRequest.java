package com.restapi.wmsservice.dto.request;

import com.restapi.wmsservice.enums.LocationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LocationRequest {

    @NotNull(message = "WAREHOUSE_ID_REQUIRED")
    Long warehouseId;

    @NotBlank(message = "LOCATION_CODE_REQUIRED")
    String code;

    String zone;

    String rack;

    Integer capacity;

    @NotNull(message = "LOCATION_STATUS_REQUIRED")
    LocationStatus status;
}
