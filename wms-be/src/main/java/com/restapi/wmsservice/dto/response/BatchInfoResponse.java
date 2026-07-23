package com.restapi.wmsservice.dto.response;

import com.restapi.wmsservice.enums.BatchStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BatchInfoResponse {
    Long id;
    Long itemId;
    String itemCode;
    String batchNo;
    LocalDateTime manufactureDate;
    LocalDateTime expiredDate;
    BatchStatus status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
