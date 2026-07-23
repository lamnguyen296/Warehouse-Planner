package com.restapi.wmsservice.mapper;

import com.restapi.wmsservice.dto.request.PlanningRequest;
import com.restapi.wmsservice.dto.response.PlanningResponse;
import com.restapi.wmsservice.entity.Planning;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {PlanningDetailMapper.class})
public interface PlanningMapper {

    @Mapping(target = "details", ignore = true)
    @Mapping(target = "workshopRequest", ignore = true)
    @Mapping(target = "planningNo", ignore = true)
    @Mapping(target = "status", ignore = true)
    Planning toPlanning(PlanningRequest request);

    @Mapping(source = "workshopRequest.id", target = "workshopRequestId")
    @Mapping(source = "workshopRequest.requestNo", target = "workshopRequestNo")
    PlanningResponse toResponse(Planning planning);

    @Mapping(target = "details", ignore = true)
    @Mapping(target = "workshopRequest", ignore = true)
    @Mapping(target = "planningNo", ignore = true)
    @Mapping(target = "status", ignore = true)
    void updatePlanning(@MappingTarget Planning planning, PlanningRequest updateRequest);
}

