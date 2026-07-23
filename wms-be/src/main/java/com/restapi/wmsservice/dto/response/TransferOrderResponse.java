package com.restapi.wmsservice.dto.response;

import com.restapi.wmsservice.enums.TransferStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransferOrderResponse {

    Long id;

    String transferNo;

    Long planningId;

    WarehouseResponse fromWarehouse;

    WarehouseResponse toWarehouse;

    ItemResponse item;

    Integer quantity;

    TransferStatus status;
}
