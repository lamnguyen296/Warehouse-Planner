package com.restapi.wmsservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecycleOrderRequest {

    Long planningDetailId;

    @NotNull(message = "From item ID is mandatory")
    Long fromItemId;

    @NotNull(message = "To item ID is mandatory")
    Long toItemId;

    @NotNull(message = "Quantity is mandatory")
    @Min(value = 1, message = "Quantity must be greater than 0")
    Integer quantity;

    @Min(value = 0, message = "Actual yield cannot be negative")
    Integer actualYield;

    LocalDateTime startTime;

    LocalDateTime finishTime;
}
