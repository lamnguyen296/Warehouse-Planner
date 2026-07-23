package com.restapi.wmsservice.mapper;

import com.restapi.wmsservice.dto.request.InventoryRequest;
import com.restapi.wmsservice.dto.response.InventoryResponse;
import com.restapi.wmsservice.entity.Inventory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    @Mapping(target = "warehouse", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "item", ignore = true)
    Inventory toInventory(InventoryRequest request);

    @Mapping(source = "warehouse.id", target = "warehouseId")
    @Mapping(source = "warehouse.code", target = "warehouseCode")
    @Mapping(source = "location.id", target = "locationId")
    @Mapping(source = "location.code", target = "locationCode")
    @Mapping(source = "item.id", target = "itemId")
    @Mapping(source = "item.code", target = "itemCode")
    InventoryResponse toInventoryResponse(Inventory inventory);

    @Mapping(target = "warehouse", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "item", ignore = true)
    void updateInventory(@MappingTarget Inventory inventory, InventoryRequest request);
}
