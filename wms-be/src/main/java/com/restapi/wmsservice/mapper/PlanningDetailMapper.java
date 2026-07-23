package com.restapi.wmsservice.mapper;

import com.restapi.wmsservice.dto.request.PlanningDetailRequest;
import com.restapi.wmsservice.dto.response.PlanningDetailResponse;
import com.restapi.wmsservice.entity.PlanningDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PlanningDetailMapper {

    @Mapping(target = "item", ignore = true)
    @Mapping(target = "planning", ignore = true)
    PlanningDetail toDetail(PlanningDetailRequest request);

    @Mapping(source = "planning.id", target = "planningId")
    @Mapping(source = "item.id", target = "itemId")
    @Mapping(source = "item.code", target = "itemCode")
    @Mapping(source = "item.name", target = "itemName")
    PlanningDetailResponse toResponse(PlanningDetail detail);

    @Mapping(target = "item", ignore = true)
    @Mapping(target = "planning", ignore = true)
    void updateDetail(@MappingTarget PlanningDetail detail, PlanningDetailRequest request);
}
