package com.restapi.wmsservice.mapper;

import com.restapi.wmsservice.dto.request.BomRequest;
import com.restapi.wmsservice.dto.response.BomResponse;
import com.restapi.wmsservice.entity.Bom;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BomMapper {

    @Mapping(target = "parentItem", ignore = true)
    @Mapping(target = "childItem", ignore = true)
    Bom toBom(BomRequest request);

    @Mapping(source = "parentItem.id", target = "parentItemId")
    @Mapping(source = "parentItem.code", target = "parentItemCode")
    @Mapping(source = "childItem.id", target = "childItemId")
    @Mapping(source = "childItem.code", target = "childItemCode")
    BomResponse toBomResponse(Bom bom);

    @Mapping(target = "parentItem", ignore = true)
    @Mapping(target = "childItem", ignore = true)
    void updateBom(@MappingTarget Bom bom, BomRequest request);
}
