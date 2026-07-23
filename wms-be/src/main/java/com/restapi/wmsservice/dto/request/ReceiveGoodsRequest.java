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
public class ReceiveGoodsRequest {

    @NotNull(message = "PURCHASE_DETAIL_ID_REQUIRED")
    Long purchaseDetailId;

    @NotNull(message = "RECEIVED_QTY_REQUIRED")
    @Min(value = 1, message = "RECEIVED_QTY_MIN")
    Integer receivedQty;

    @NotNull(message = "WAREHOUSE_ID_REQUIRED")
    Long warehouseId;

    @NotNull(message = "LOCATION_ID_REQUIRED")
    Long locationId;
}
