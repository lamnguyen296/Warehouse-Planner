package com.restapi.wmsservice.mapper;

import com.restapi.wmsservice.dto.request.PurchaseRequestDetailRequest;
import com.restapi.wmsservice.dto.response.PurchaseRequestDetailResponse;
import com.restapi.wmsservice.entity.PurchaseRequestDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {ItemMapper.class})
public interface PurchaseRequestDetailMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "purchaseRequest", ignore = true)
    @Mapping(target = "item", ignore = true)
    @Mapping(target = "receivedQuantity", ignore = true)
    PurchaseRequestDetail toEntity(PurchaseRequestDetailRequest request);

    PurchaseRequestDetailResponse toResponse(PurchaseRequestDetail entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "purchaseRequest", ignore = true)
    @Mapping(target = "item", ignore = true)
    @Mapping(target = "receivedQuantity", ignore = true)
    void updateEntity(@MappingTarget PurchaseRequestDetail entity, PurchaseRequestDetailRequest request);
}
