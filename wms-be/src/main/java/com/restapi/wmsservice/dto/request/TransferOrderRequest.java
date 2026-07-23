package com.restapi.wmsservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransferOrderRequest {

    Long planningId;

    @NotNull(message = "From warehouse ID is mandatory")
    Long fromWarehouseId;

    @NotNull(message = "To warehouse ID is mandatory")
    Long toWarehouseId;

    @NotNull(message = "Item ID is mandatory")
    Long itemId;

    @NotNull(message = "Quantity is mandatory")
    @Min(value = 1, message = "Quantity must be greater than 0")
    Integer quantity;
}
