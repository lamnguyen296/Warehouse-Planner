package com.restapi.wmsservice.mapper;

import com.restapi.wmsservice.dto.request.InventoryTransactionRequest;
import com.restapi.wmsservice.dto.response.InventoryTransactionResponse;
import com.restapi.wmsservice.entity.InventoryTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {InventoryTransactionDetailMapper.class})
public interface InventoryTransactionMapper {

    @Mapping(target = "fromWarehouse", ignore = true)
    @Mapping(target = "toWarehouse", ignore = true)
    @Mapping(target = "details", ignore = true)
    InventoryTransaction toTransaction(InventoryTransactionRequest request);

    @Mapping(source = "fromWarehouse.id", target = "fromWarehouseId")
    @Mapping(source = "fromWarehouse.code", target = "fromWarehouseCode")
    @Mapping(source = "toWarehouse.id", target = "toWarehouseId")
    @Mapping(source = "toWarehouse.code", target = "toWarehouseCode")
    InventoryTransactionResponse toResponse(InventoryTransaction transaction);
}
