package com.restapi.wmsservice.mapper;

import com.restapi.wmsservice.dto.request.WarehouseRequest;
import com.restapi.wmsservice.dto.response.WarehouseResponse;
import com.restapi.wmsservice.entity.Warehouse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface WarehouseMapper {

    @Mapping(target = "locations", ignore = true)
    Warehouse toWarehouse(WarehouseRequest request);

    WarehouseResponse toWarehouseResponse(Warehouse warehouse);

    @Mapping(target = "locations", ignore = true)
    void updateWarehouse(@MappingTarget Warehouse warehouse, WarehouseRequest request);
}
