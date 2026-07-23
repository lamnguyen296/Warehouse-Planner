package com.restapi.wmsservice.mapper;

import com.restapi.wmsservice.dto.request.AssemblyOrderRequest;
import com.restapi.wmsservice.dto.response.AssemblyOrderResponse;
import com.restapi.wmsservice.entity.AssemblyOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {ItemMapper.class})
public interface AssemblyOrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "assemblyNo", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "planningDetail", ignore = true)
    @Mapping(target = "setItem", ignore = true)
    AssemblyOrder toEntity(AssemblyOrderRequest request);

    @Mapping(target = "planningDetailId", source = "planningDetail.id")
    AssemblyOrderResponse toResponse(AssemblyOrder entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "assemblyNo", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "planningDetail", ignore = true)
    @Mapping(target = "setItem", ignore = true)
    void updateEntity(@MappingTarget AssemblyOrder entity, AssemblyOrderRequest request);
}
