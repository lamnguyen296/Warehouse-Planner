package com.restapi.wmsservice.mapper;

import com.restapi.wmsservice.dto.response.InventoryBatchStockResponse;
import com.restapi.wmsservice.entity.InventoryBatchStock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryBatchStockMapper {

    @Mapping(source = "inventory.id", target = "inventoryId")
    @Mapping(source = "batchInfo.id", target = "batchId")
    @Mapping(source = "batchInfo.batchNo", target = "batchNo")
    InventoryBatchStockResponse toResponse(InventoryBatchStock inventoryBatchStock);
}
