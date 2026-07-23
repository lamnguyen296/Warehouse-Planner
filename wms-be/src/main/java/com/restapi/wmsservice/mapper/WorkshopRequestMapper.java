package com.restapi.wmsservice.mapper;

import com.restapi.wmsservice.dto.request.WorkshopRequestRequest;
import com.restapi.wmsservice.dto.response.WorkshopRequestResponse;
import com.restapi.wmsservice.entity.WorkshopRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {WorkshopRequestDetailMapper.class})
public interface WorkshopRequestMapper {

    @Mapping(target = "details", ignore = true)
    @Mapping(target = "plannings", ignore = true)
    @Mapping(target = "requestNo", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    WorkshopRequest toRequest(WorkshopRequestRequest request);

    WorkshopRequestResponse toResponse(WorkshopRequest request);

    @Mapping(target = "details", ignore = true)
    @Mapping(target = "plannings", ignore = true)
    @Mapping(target = "requestNo", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateRequest(@MappingTarget WorkshopRequest request, WorkshopRequestRequest updateRequest);
}
