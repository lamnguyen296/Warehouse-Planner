package com.restapi.wmsservice.mapper;

import com.restapi.wmsservice.dto.request.PurchaseRequestRequest;
import com.restapi.wmsservice.dto.response.PurchaseRequestResponse;
import com.restapi.wmsservice.entity.PurchaseRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {PurchaseRequestDetailMapper.class})
public interface PurchaseRequestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "requestNo", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "planningDetail", ignore = true)
    @Mapping(target = "details", ignore = true)
    PurchaseRequest toEntity(PurchaseRequestRequest request);

    @Mapping(target = "planningDetailId", source = "planningDetail.id")
    PurchaseRequestResponse toResponse(PurchaseRequest entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "requestNo", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "planningDetail", ignore = true)
    @Mapping(target = "details", ignore = true)
    void updateEntity(@MappingTarget PurchaseRequest entity, PurchaseRequestRequest request);
}
