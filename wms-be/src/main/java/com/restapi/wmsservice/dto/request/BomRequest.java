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
public class BomRequest {

    @NotNull(message = "PARENT_ITEM_REQUIRED")
    Long parentItemId;

    @NotNull(message = "CHILD_ITEM_REQUIRED")
    Long childItemId;

    @NotNull(message = "QUANTITY_REQUIRED")
    @Min(value = 1, message = "QUANTITY_MIN")
    Integer quantity;

    Integer priority;
}
