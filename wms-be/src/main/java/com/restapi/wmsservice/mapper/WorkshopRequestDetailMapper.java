package com.restapi.wmsservice.mapper;

import com.restapi.wmsservice.dto.request.WorkshopRequestDetailRequest;
import com.restapi.wmsservice.dto.response.WorkshopRequestDetailResponse;
import com.restapi.wmsservice.entity.WorkshopRequestDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface WorkshopRequestDetailMapper {

    @Mapping(target = "item", ignore = true)
    @Mapping(target = "workshopRequest", ignore = true)
    WorkshopRequestDetail toDetail(WorkshopRequestDetailRequest request);

    @Mapping(source = "workshopRequest.id", target = "workshopRequestId")
    @Mapping(source = "item.id", target = "itemId")
    @Mapping(source = "item.code", target = "itemCode")
    @Mapping(source = "item.name", target = "itemName")
    WorkshopRequestDetailResponse toResponse(WorkshopRequestDetail detail);

    @Mapping(target = "item", ignore = true)
    @Mapping(target = "workshopRequest", ignore = true)
    void updateDetail(@MappingTarget WorkshopRequestDetail detail, WorkshopRequestDetailRequest request);
}
