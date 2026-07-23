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
public class PurchaseRequestDetailRequest {

    @NotNull(message = "Item ID is mandatory")
    Long itemId;

    @NotNull(message = "Quantity is mandatory")
    @Min(value = 1, message = "Quantity must be greater than 0")
    Integer quantity;
}
