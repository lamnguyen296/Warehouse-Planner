package com.restapi.wmsservice.dto.request;

import com.restapi.wmsservice.enums.BatchStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BatchInfoRequest {

    @NotNull(message = "ITEM_ID_REQUIRED")
    Long itemId;

    @NotBlank(message = "BATCH_NO_REQUIRED")
    String batchNo;

    LocalDateTime manufactureDate;

    LocalDateTime expiredDate;

    @NotNull(message = "BATCH_STATUS_REQUIRED")
    BatchStatus status;
}
