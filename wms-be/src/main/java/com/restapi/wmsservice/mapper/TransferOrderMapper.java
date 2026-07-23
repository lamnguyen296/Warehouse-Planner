package com.restapi.wmsservice.mapper;

import com.restapi.wmsservice.dto.request.TransferOrderRequest;
import com.restapi.wmsservice.dto.response.TransferOrderResponse;
import com.restapi.wmsservice.entity.TransferOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {ItemMapper.class, WarehouseMapper.class})
public interface TransferOrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "transferNo", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "planning", ignore = true)
    @Mapping(target = "fromWarehouse", ignore = true)
    @Mapping(target = "toWarehouse", ignore = true)
    @Mapping(target = "item", ignore = true)
    TransferOrder toEntity(TransferOrderRequest request);

    @Mapping(target = "planningId", source = "planning.id")
    TransferOrderResponse toResponse(TransferOrder entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "transferNo", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "planning", ignore = true)
    @Mapping(target = "fromWarehouse", ignore = true)
    @Mapping(target = "toWarehouse", ignore = true)
    @Mapping(target = "item", ignore = true)
    void updateEntity(@MappingTarget TransferOrder entity, TransferOrderRequest request);
}
