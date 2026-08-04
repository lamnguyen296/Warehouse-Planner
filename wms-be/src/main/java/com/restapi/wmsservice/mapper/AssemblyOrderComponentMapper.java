package com.restapi.wmsservice.mapper;

import com.restapi.wmsservice.dto.response.AssemblyOrderComponentResponse;
import com.restapi.wmsservice.entity.AssemblyOrderComponent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AssemblyOrderComponentMapper {

    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "itemCode", source = "item.code")
    @Mapping(target = "itemName", source = "item.name")
    AssemblyOrderComponentResponse toResponse(AssemblyOrderComponent component);
}
