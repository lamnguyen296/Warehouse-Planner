package com.restapi.wmsservice.mapper;

import com.restapi.wmsservice.dto.request.LocationRequest;
import com.restapi.wmsservice.dto.response.LocationResponse;
import com.restapi.wmsservice.entity.Location;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    @Mapping(target = "warehouse", ignore = true)
    Location toLocation(LocationRequest request);

    @Mapping(source = "warehouse.id", target = "warehouseId")
    @Mapping(source = "warehouse.code", target = "warehouseCode")
    LocationResponse toLocationResponse(Location location);

    @Mapping(target = "warehouse", ignore = true)
    void updateLocation(@MappingTarget Location location, LocationRequest request);
}
