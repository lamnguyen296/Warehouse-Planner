package com.restapi.wmsservice.dto.request;

import com.restapi.wmsservice.enums.ItemStatus;
import com.restapi.wmsservice.enums.ItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemRequest {

    @NotBlank(message = "ITEM_CODE_REQUIRED")
    String code;

    @NotBlank(message = "ITEM_NAME_REQUIRED")
    String name;

    @NotNull(message = "ITEM_TYPE_REQUIRED")
    ItemType itemType;

    String unit;

    String description;

    @NotNull(message = "ITEM_STATUS_REQUIRED")
    ItemStatus status;
}
