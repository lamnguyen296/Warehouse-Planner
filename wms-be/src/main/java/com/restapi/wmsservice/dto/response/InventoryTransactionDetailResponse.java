package com.restapi.wmsservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryTransactionDetailResponse {
    Long id;
    Long itemId;
    String itemCode;
    Long batchId;
    String batchNo;
    Long locationFrom;
    String locationFromCode;
    Long locationTo;
    String locationToCode;
    Integer quantity;
}
