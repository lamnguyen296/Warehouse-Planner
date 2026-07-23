package com.restapi.wmsservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompleteAssemblyRequest {

    @NotNull(message = "Warehouse ID is required")
    Long warehouseId;

    @NotNull(message = "Location ID is required")
    Long locationId;
}
