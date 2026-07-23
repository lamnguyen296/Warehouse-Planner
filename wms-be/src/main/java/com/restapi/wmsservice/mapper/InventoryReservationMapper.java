package com.restapi.wmsservice.mapper;

import com.restapi.wmsservice.dto.request.InventoryReservationRequest;
import com.restapi.wmsservice.dto.response.InventoryReservationResponse;
import com.restapi.wmsservice.entity.InventoryReservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface InventoryReservationMapper {

    @Mapping(target = "planningDetail", ignore = true)
    @Mapping(target = "item", ignore = true)
    @Mapping(target = "warehouse", ignore = true)
    @Mapping(target = "inventory", ignore = true)
    InventoryReservation toReservation(InventoryReservationRequest request);

    @Mapping(source = "planningDetail.id", target = "planningDetailId")
    @Mapping(source = "item.id", target = "itemId")
    @Mapping(source = "item.code", target = "itemCode")
    @Mapping(source = "warehouse.id", target = "warehouseId")
    @Mapping(source = "warehouse.code", target = "warehouseCode")
    InventoryReservationResponse toResponse(InventoryReservation reservation);

    @Mapping(target = "planningDetail", ignore = true)
    @Mapping(target = "item", ignore = true)
    @Mapping(target = "warehouse", ignore = true)
    @Mapping(target = "inventory", ignore = true)
    void updateReservation(@MappingTarget InventoryReservation reservation, InventoryReservationRequest request);
}
