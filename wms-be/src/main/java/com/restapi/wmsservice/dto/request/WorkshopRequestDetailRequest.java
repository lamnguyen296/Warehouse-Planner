package com.restapi.wmsservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkshopRequestDetailRequest {

    @NotNull(message = "ITEM_ID_REQUIRED")
    Long itemId;

    @NotNull(message = "QUANTITY_REQUIRED")
    @Min(value = 1, message = "INVALID_QUANTITY")
    Integer quantity;
}

