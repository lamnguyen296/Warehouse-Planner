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
public class CompleteRecycleRequest {

    /** Số lượng FINISHED_COMPONENT thực tế tạo ra (có thể nhỏ hơn input quantity do yield loss). */
    @NotNull(message = "ACTUAL_YIELD_REQUIRED")
    @Min(value = 0, message = "ACTUAL_YIELD_MIN")
    Integer actualYield;
}
