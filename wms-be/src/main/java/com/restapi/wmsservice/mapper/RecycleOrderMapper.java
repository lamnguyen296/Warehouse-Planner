package com.restapi.wmsservice.mapper;

import com.restapi.wmsservice.dto.request.RecycleOrderRequest;
import com.restapi.wmsservice.dto.response.RecycleOrderResponse;
import com.restapi.wmsservice.entity.RecycleOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {ItemMapper.class})
public interface RecycleOrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "orderNo", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "planningDetail", ignore = true)
    @Mapping(target = "fromItem", ignore = true)
    @Mapping(target = "toItem", ignore = true)
    @Mapping(target = "expectedYield", ignore = true)
    @Mapping(target = "conversionRatio", ignore = true)
    RecycleOrder toEntity(RecycleOrderRequest request);

    @Mapping(target = "planningDetailId", source = "planningDetail.id")
    RecycleOrderResponse toResponse(RecycleOrder entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "orderNo", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "planningDetail", ignore = true)
    @Mapping(target = "fromItem", ignore = true)
    @Mapping(target = "toItem", ignore = true)
    @Mapping(target = "expectedYield", ignore = true)
    @Mapping(target = "conversionRatio", ignore = true)
    void updateEntity(@MappingTarget RecycleOrder entity, RecycleOrderRequest request);
}
